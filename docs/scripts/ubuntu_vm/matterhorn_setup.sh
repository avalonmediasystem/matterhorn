#!/bin/bash

# Initialization
INST_DIR=/home/opencast
CONF_DIR=/opt/matterhorn/felix/conf
MOTD_FILE=/etc/motd.tail
MY_OS=`uname -sr`

show_stat()
{
cat >&1 <<END
********************************************
******** Starting Matterhorn Setup ********
********************************************
** Installing third party components
**
** VM OS: $MY_OS
** VM IP: $MY_IP
**
** Matterhorn is installed in:
**    Home:    /usr/local/felix-framework-2.0.1
**    Bundles: /usr/local/felix-framework-2.0.1/load
**    Config:  /usr/local/felix-framework-2.0.1/conf
**
** For further information, please visit
**   http://www.opencastproject.org
********************************************

For a complete list of 3rd party tools, please visit:
  http://wiki.opencastproject.org/iconfluence/display/open/3rd+Party+Licensesi+and+Software

** PLEASE NOTE: You may be prompted for the login password to authorize the installation script.
END
}

install_3p ()
{
  echo "Installing 3rd party tools.  This process will take several minutes..."

  cd $INST_DIR
  sudo apt-get -y --force-yes install curl ntp
  sudo apt-get -y --force-yes install openssh-server openssh-client
  sudo apt-get -y --force-yes install build-essential zlib1g-dev patch byacc

  #opencv
  sudo apt-get -y --force-yes install libcv1 libcv-dev opencv-doc

  #install media info
  wget http://downloads.sourceforge.net/zenlib/libzen0_0.4.8-1_i386.Ubuntu_9.04.deb
  sudo dpkg -i libzen0_0.4.8-1_i386.Ubuntu_9.04.deb
  rm -f libzen0_0.4.8-1_i386.Ubuntu_9.04.deb

  #ocr support
  echo "ocr support"
  sudo apt-get -y --force-yes install libpng12-dev libjpeg62-dev libtiff4-dev
  sudo apt-get -y --force-yes install tesserat-ocr
  cd /usr/share/tesseract-ocr
  #install english language file
  sudo curl http://tesseract-ocr.googlecode.com/files/tesseract-2.00.eng.tar.gz | sudo tar xz
  cd tessdata
  sudo chmod 755 *
  
}

install_ffmpeg ()
{
  echo "Installing ffmpeg and related libraries."

  cd $INST_DIR

  sudo apt-get -y --force-yes update
  sudo apt-get -y --force-yes install build-essential subversion git-core checkinstall yasm texi2html libfaac-dev libfaad-dev libmp3lame-dev libopencore-amrnb-dev libopencore-amrwb-dev libsdl1.2-dev libx11-dev libxfixes-dev libxvidcore4-dev zlib1g-dev
  sudo apt-get -y --force-yes install libtheora-dev

  cd
  wget ftp://ftp.videolan.org/pub/videolan/x264/snapshots/x264-snapshot-20100214-2245.tar.bz2
  tar xvf x264-snapshot-20100214-2245.tar.bz2
  cd x264-snapshot-20100214-2245/
  sudo ./configure
  sudo make
  sudo make install
  
  cd
  svn checkout -r 20641 svn://svn.ffmpeg.org/ffmpeg/trunk ffmpeg
  cd ffmpeg
  rm -rf libswscale 
  svn checkout -r 30380 svn://svn.ffmpeg.org/mplayer/trunk/libswscale libswscale

  ./configure --enable-gpl --enable-version3 --enable-nonfree --enable-postproc --enable-pthreads --enable-libfaac --enable-libfaad --enable-libmp3lame --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-libtheora --enable-libx264 --enable-libxvid --enable-x11grab
  make
  sudo checkinstall --pkgname=ffmpeg --pkgversion "4:0.5+svn`date +%Y%m%d`" --backup=no --default
  hash ffmpeg
}

start_mh ()
{
  echo "Starting Matterhorn..."

  FELIX=felix
  FELIX_DIR=/opt/matterhorn/$FELIX

  export OC=/opt/matterhorn
  export FELIX_HOME=/opt/matterhorn/felix
  export RED5_HOME=/opt/matterhorn/red5
  export M2_REPO=/home/opencast/.m2/repository
  export OC_URL=http://opencast.jira.com/svn/MH/trunk/
  export FELIX_URL=http://apache.mirror.iweb.ca/felix/felix-framework-2.0.1.tar.gz
  export JAVA_HOME=/usr/lib/jvm/java-6-sun
  export MAVEN_OPTS="-Xms256m -Xmx512m -XX:PermSize=64m -XX:MaxPermSize=128m"

  cd $INST_DIR

  /home/opencast/startup.sh

  echo "" | sudo tee -a $MOTD_FILE
  echo "********************************************" | sudo tee -a $MOTD_FILE
  echo "** Matterhorn console is at http://$MY_IP:8080" | sudo tee -a $MOTD_FILE
  echo "**" | sudo tee -a $MOTD_FILE
  echo "** Matterhorn is installed in:" | sudo tee -a $MOTD_FILE
  echo "**    Home:    /opt/matterhorn/felix" | sudo tee -a $MOTD_FILE
  echo "**    Bundles: /opt/matterhorn/felix/load" | sudo tee -a $MOTD_FILE
  echo "**    Config:  /opt/matterhorn/felix/conf" | sudo tee -a $MOTD_FILE
  echo "********************************************" | sudo tee -a $MOTD_FILE

  # remove matterhorn setup script
  sudo mv /etc/profile.d/matterhorn_setup.sh /home/opencast/.
}

############################### START HERE ###############################
# Turn off screensaver
setterm -blank 0 

# Wait for network connection
for ntime in 1 2 3 4 5 6 7 8 9 10
do
  MY_IP=`ifconfig | grep "inet addr:" | grep -v 127.0.0.1 | awk '{print $2}' | cut -d':' -f2`
  if [ ! -z $MY_IP ]; then
    break;
  fi
  echo "Waiting for network connection..."
  sleep 5
done

# Did we get connected?
if [ -z $MY_IP ]; then
  echo "** ERROR: Could not acquire IP address for this VM."
  echo "Edit file /etc/udev/rules.d/70-persistent-net.rules and remove all uncommented"
  echo "lines, and restart the VM.  For more info see Functionality section of FAQ at"
  echo "http://opencast.jira.com/wiki/display/MH/Release+0.5+FAQ"
else
  # connected, start main task
  show_stat
  echo "******** OPTIONS HAVE CHANGED, PLEASE READ CAREFULLY *********"

  # Need to get a server name, not just y/n
  proxsrv=y
  while [ ${#proxsrv} -gt 0 ] && [ ${#proxsrv} -lt 8 ]
  do
    echo "**** To set up a proxy server, please enter the URL or press enter []?"
    read proxsrv
  done

  # proxy server?
  if [ ${#proxsrv} -gt 7 ]; then
    echo "http_proxy=$proxsrv" | sudo tee /etc/profile.d/httpproxy.sh
    echo "export http_proxy" | sudo tee -a /etc/profile.d/httpproxy.sh
    sudo chown opencast /etc/profile.d/httpproxy.sh
    sudo chgrp opencast /etc/profile.d/httpproxy.sh
    sudo chmod 755 /etc/profile.d/httpproxy.sh
    export http_proxy=$proxsrv
  else
    echo "No proxy server specified."
  fi

  echo "**** Default keyboard is US; Do you want to reconfigure? [y/N]"
  read kbresp

  # Need to get a server name, not just y/n
  ntpsrv=y
  while [ ${#ntpsrv} -gt 0 ] && [ ${#ntpsrv} -lt 8 ]
  do
    echo "**** To set up a different NTP server, please enter the URL or press enter [ntp.ubuntu.com]?"
    read ntpsrv
  done

  echo "**** Do you want to change the timezone on this VM? [y/N]"
  read changetz

  echo "**** Do you want to install 3rd party tools? [Y/n]"
  read p3resp

  echo "**** Do you want to install ffmpeg? [Y/n]"
  read ffresp

  echo "**** Do you want to install OpenCaps? [y/N]"
  read opencaps
  if [ $opencaps = "y" ] || [ $opencaps = "Y" ]; then
    echo "**** Install OpenCaps as Matterhorn plugin only? [Y/n]"
    read opencapsminimal
  fi


  # update felix config (url)
  sed -i "s/http:\/\/localhost:8080/http:\/\/$MY_IP:8080/" $CONF_DIR/config.properties

  # update capture properties
  # sed -i "s/http:\/\/localhost:8080/http:\/\/$MY_IP:8080/" /opencast/config/capture.properties

  # Reconfigure Keyboard?
  if [ $kbresp = "y" ] || [ $kbresp = "Y" ]; then
    sudo dpkg-reconfigure console-setup
  else
    echo "Keeping default keybord configuration."
  fi
  
  echo "Installation wget, subversion and git."
  sudo apt-get -y --force-yes install wget subversion git-core

  # Change Timezone?
  if [ $changetz = "y" ] || [ $changetz = "Y" ]; then
    tzselect
  else
    echo "Timezone will NOT be changed."
  fi

  # Install 3P tools?
  if [ $p3resp != "n" ] && [ $p3resp != "N" ]; then
    install_3p
  else
    echo "3rd party tools will NOT be installed."
  fi

  # Install ffmpeg?
  if [ $ffresp != "n" ] && [ $ffresp != "N" ]; then
    install_ffmpeg
  else
    echo "ffmpeg will NOT be installed."
  fi
  
  # Install opencaps?
  if [ $opencaps = "y" ] || [ $opencaps = "Y" ]; then
    if [ $opencapsminimal = "n" ] || [ $opencaps = "N" ]; then
      /home/opencast/opencaps.sh
    else
      /home/opencast/opencaps_matterhorn_only.sh
    fi
  else
    echo "opencaps will NOT be installed."
  fi


  # doing some additional setups
  sudo update-java-alternatives -s java-6-sun
  sudo chown -R 1000:1000 /home/opencast

  # ntp server?
  if [ ${#ntpsrv} -gt 7 ]; then
    sudo sed -i "s/ntp.ubuntu.com/$ntpsrv/" /etc/ntp.conf
  else
    echo "Using default NTP server: ntp.ubuntu.com"
  fi

  # restart ntp, just to make sure that time will be synchronized
  sudo /etc/init.d/ntp restart

  start_mh

  echo "done."
fi

