<?php
# sempro.php
# Copyright (c) 2007 by Francesco Piovano http://www.replay.ethz.ch/
# modifications by Christoph Driessen <ced@neopoly.de> 2008
# SOAP interface documentation of ETH Zurich VVZ under
# https://soap.bi.id.ethz.ch/soapvvz/help/default.htm
#
# select code,titel,room,stime,etime from lerneinheit join rooms using (lid) ;
# select code,titel,doz.* from lerneinheit join lh_doz using (lid) join doz using (doz_id) ;

// $my_version = "simulation=true";
$my_version = "replay_sempro_soap_client_v3";

# Creates the data base tables
function make_tables()
{
  $coooo = mysql_connect("mysqlweb.ethz.ch","<USER>","<PASSWORD>");
  @mysql_select_db("replay") or die( "Unable to select database");

  $q1 = "drop table if exists SEMESTER, LERNEINHEIT, ROOM, DOZ, LERNEINHEIT_DOZ";
  $q2 = "create table SEMESTER (CODE char(5) not null primary key,
                                LONG_DESC varchar(100),
                                START date,
                                MIDDLE date,
                                END date)";
  $q3 = "create table LERNEINHEIT (L_ID int not null primary key,
                                   SEMKEZ char(5),
                                   LK_EINHEIT_ID int,
                                   CODE varchar(12),
                                   TITEL varchar(100),
                                   TITEL_En varchar(100),
                                   BESONDERES varchar(4000),
                                   BESONDERES_EN varchar(4000),
                                   SUPPLEMENT varchar(4000),
                                   SUPPLEMENT_EN varchar(4000),
                                   INHALT varchar(4000),
                                   INHALT_EN varchar(4000),
                                   LERNZIEL varchar(4000),
                                   LERNZIEL_EN varchar(4000))";
  $q4 = "create table ROOM (L_ID int not null,
                            ROOM_ID int,
                            NAME varchar(10),
                            DAY_IN_WEEK int,
                            START_TIME varchar(5),
                            END_TIME varchar(5),
                            WEEK_DELTA_START int,
                            WEEK_DELTA_END int)";
  $q5 = "create table DOZ (DOZ_ID int not null primary key,
                           SURNAME varchar(30),
                           NAME varchar(30))";
  $q6 = "create table LERNEINHEIT_DOZ (L_ID int not null,
                                       DOZ_ID int not null,
                                       unique index (L_ID, DOZ_ID))";

  $res = mysql_query($q1);
  if (!$res)
    echo "Drop tables nok ... \n" . $q1 . "\n" . mysql_error();

  $res = mysql_query($q2);
  if (!$res)
    echo "Create SEMESTER nok ... \n" . $q2 . "\n" . mysql_error();

  $res = mysql_query($q3);
  if (!$res)
    echo "Create LERNEINHEIT nok ... \n" . $q3 . "\n" . mysql_error();

  $res = mysql_query($q4);
  if (!$res)
    echo "Create ROOM nok ... \n" . $q4 . "\n" . mysql_error();

  $res = mysql_query($q5);
  if (!$res)
    echo "Create DOZ nok ... \n" . $q5 . "\n" . mysql_error();

  $res = mysql_query($q6);
  if (!$res)
    echo "Create LERNEINHEIT_DOZ nok ... \n" . $q6 . "\n" . mysql_error();

  $charset = mysql_client_encoding($coooo);
  printf ("Current character set is = \n", $charset);
//exit (0);

}

function dbwrite_doz($lern_id, $doz)
{
  if (isset ($lern_id) && is_array ($doz) && (count($doz) > 0))
  {
//    var_dump($doz);
    foreach ($doz as $d)
    {
      $sur = addslashes($d->name);
      $nam = addslashes($d->vorname);
      $query = "insert ignore into DOZ values
                ('$d->dozide',
                _utf8'$sur',
                _utf8'$nam')";
      $res = mysql_query($query);
      if (!$res)
        echo "Insert DOZ nok ... \n" . $query . "\n" . mysql_error();

      $query = "insert ignore into LERNEINHEIT_DOZ values
                ('$lern_id',
                 '$d->dozide')";
      $res = mysql_query($query);
      if (!$res)
        echo "Insert LERNEINHEIT_DOZ nok ... \n" . $query . "\n" . mysql_error();

    }
  }
}

function dbwrite_room($lern_id, $rooms)
{
  if (isset ($lern_id) && is_array ($rooms) && (count($rooms) > 0))
  {
    foreach ($rooms as $r)
    {
      $roomName = $r->gebaeude . $r->geschoss . $r->raum;
      $query = "insert ignore into ROOM values
                ('$lern_id',
                 '$r->veranstaltungsortid',
                 '$roomName',
                 '$r->wochentag',
                 '$r->uhrzeitVon',
                 '$r->uhrzeitBis',
                 '$r->wochendeltabeginn',
                 '$r->wochendeltaende')";
      $res = mysql_query($query);

      if (!$res)
        echo "insert room nok ... \n" . $query . "\n" . mysql_error();
    }
  }
}

function dbwrite_semesters(&$a)
{
  foreach ($a as $value)
  {
    if (isset ($value->semkez) && isset ($value->semesterLang))
    {
    var_dump($value);
      $query = "insert ignore into SEMESTER values
                ('$value->semkez',
                 _utf8'$value->semesterlang',
                 '$value->semesterbeginn',
                 '$value->semestermitte'
                 '$value->semesterende')";
      $res = mysql_query($query);

      if (!$res)
        echo "Insert SEMESTER nok ... \n" . $query . "\n" . mysql_error();
    }
  }
}

# WsLerneinheit
function check_lern(&$l)
{
  if (isset ($l))
  {
      $id = $l->ID;
      $tit = addslashes($l->titel);
      $titen = addslashes($l->titelEnglisch);
      $bes = addslashes($l->besonderes);
      $besen = addslashes($l->besonderesEnglisch);
      $sup = addslashes($l->diplomaSupplement);
      $supen = addslashes($l->diplomaSupplementEnglisch);
      $inh = addslashes($l->inhalt);
      $inhen = addslashes($l->inhaltEnglisch);
      $ler = addslashes($l->lernziel);
      $leren = addslashes($l->lernzielEnglisch);
      $query = "insert ignore into LERNEINHEIT values
                ('$id',
                 _utf8'$l->semkez',
                 '$l->LKEinheitID',
                 '$l->code',
                 _utf8'$tit', _utf8'$titen',
                 _utf8'$bes', _utf8'$besen',
                 _utf8'$sup', _utf8'$supen',
                 _utf8'$inh', _utf8'$inhen',
                 _utf8'$ler', _utf8'$leren')";
      $res = mysql_query($query);

      echo "Add LERNEINHEIT $id ", $res ? "ok" : "nok", "\n";
      if (!$res)
        echo "Insert LERNEINHEIT nok ... \n" . $query . "\n" . mysql_error();
      # WsLehrveranstaltung
      $sons = $l->lehrveranstaltungen;
      if (isset ($sons) && is_array ($sons) && (count($sons) > 0))
      foreach ($sons as $son)
      {
        # 1 = takes place
        if ($son->findetstatt == 1)
        {
          dbwrite_room($id, $son->belegungsSerien);
          dbwrite_doz($id, $son->lehrveranstalter);
        }
      }
    }
}

function get_semesters()
{
  # old URL: https://soap.bi.id.ethz.ch/soapvvz
  # new URL: https://www.bi.id.ethz.ch/soapvvz (as of 12.06.2009)
  $client = new SoapClient("https://www.bi.id.ethz.ch/soapvvz/services/v-1-0?wsdl",
        array('location' => "https://www.bi.id.ethz.ch/soapvvz/services/v-1-0"));
  echo("\nChecking ver of soap service == 1.5 ...");
  $soap_version = $client->getInterfaceVersion($my_version);
  if (strncmp($soap_version, "1.5", 3))
  {
    echo ("Failed.  Exiting\n" . $soap_version);
    exit (1);
  }
  else
    echo ("OK\n");
  $semester_list = $client->retrieveAvailableSemkez($my_version);
  if (isset($semester_list) && is_array($semester_list))
  {
    dbwrite_semesters($semester_list);
    return ($client);
  }
  return (NULL);
}

function check_abschnitt(&$abs_list)
{
  if (is_array($abs_list))
    foreach ($abs_list as $ab)
    {
      if (isset($ab) && isset($ab->abschnittElementTyp))
      {
        $typ = $ab->abschnittElementTyp;
//        if ($typ == 1) {  // abschnittReferenz
//        }
        if ($typ == 2) {  // lerneiheit
          $lern = $ab->lerneiheit;
          check_lern($lern);
        }
        if ($typ == 3) {  // abschnittDaten
          $son = $ab->kinderElemente;
          check_abschnitt($son);
      }
    }
  }
  else
    echo "abs_list not an array !\n";
}

function get_section(&$sc, $ksem)
{
  $count = 0;
  $section_list = $sc->retrieveToplevelAbschnittId_BySemkez($my_version, $ksem);
  if (isset($section_list) && is_array($section_list))
  {
    foreach ($section_list as $id)
    {
      $abs_list = $sc->retrieveAbschnitt_byId($my_version, $id);

//    var_dump($abs_list);
      check_abschnitt($abs_list);
      $count ++;
//      if ($count > 20)
//        exit(0);
      sleep (3);
    }
  }
  return (false);
}

#
# MAIN
#

make_tables();
$sc = get_semesters();
if (isset ($sc))
{
  get_section($sc, "2008W");
}
else
  echo "Failed\n";
mysql_close();

?>
