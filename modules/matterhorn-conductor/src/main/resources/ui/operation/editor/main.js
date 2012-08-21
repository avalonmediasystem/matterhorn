var sourcesInternal = '' +
    '<source src="media/video.mp4" type=\'video/mp4; codecs="avc1.42E01E, mp4a.40.2"\'>' +
    '<source src="media/video.webm" type=\'video/webm; codecs="vp8, vorbis"\'>' +
    '<source src="media/video.ogg" type=\'video/ogg; codecs="theora, vorbis"\'>'+
    'Your browser does not support HTML5 video.';
var sourcesExternal = '' +
    '<source src="http://video-js.zencoder.com/oceans-clip.mp4" type=\'video/mp4; codecs="avc1.42E01E, mp4a.40.2"\'>' +
    '<source src="http://video-js.zencoder.com/oceans-clip.webm" type=\'video/webm; codecs="vp8, vorbis"\'>' +
    '<source src="http://video-js.zencoder.com/oceans-clip.ogg" type=\'video/ogg; codecs="theora, vorbis"\'>' +
    'Your browser does not support HTML5 video.';
var theme1 = 'default';
var theme2 = 'default';
var theme3 = 'default';
var theme4 = 'default';
var theme5 = 'default';
var theme6 = 'default';
var previewImage1 = 'files/img/preview1.png';
var previewImage2 = 'files/img/preview2.png';
var previewImage3 = 'files/img/preview1.png';
var previewImage4 = 'files/img/preview2.png';
var previewImage5 = 'files/img/preview1.png';
var previewImage6 = 'files/img/preview2.png';
var autoplay1 = 'false';
var autoplay2 = 'false';
var autoplay3 = 'false';
var autoplay4 = 'false';
var autoplay5 = 'false';
var autoplay6 = 'false';
var subtitle1 = 'media/captions/video_sub_en.srt';
var subtitle2 = '';
var subtitle3 = '';
var subtitle4 = '';
var subtitle5 = '';
var subtitle6 = 'media/captions/video_sub_en.srt';
var controls1 = 'true';
var controls2 = 'true';
var controls3 = 'true';
var controls4 = 'false';
var controls5 = 'true';
var controls6 = 'false';

jQuery(document).ready(function()
		       {
			   (function($)
			    {
				/*
				$('#video_player_1').html(sourcesInternal);
				$('#video_player_2').html(sourcesInternal);
				$('#video_player_3').html(sourcesInternal);
				$('#video_player_4').html(sourcesInternal);
				$('#video_player_5').html(sourcesInternal);
				$('#video_player_6').html(sourcesInternal);
				*/
				$('#video_player_1').html(sourcesExternal);
				$('#video_player_2').html(sourcesExternal);
				$('#video_player_3').html(sourcesExternal);
				$('#video_player_4').html(sourcesExternal);
				$('#video_player_5').html(sourcesExternal);
				$('#video_player_6').html(sourcesExternal);
				var pl1 = $('#video_player_1').mhPlayer({
				    theme: theme1,
				    autoplay: autoplay1,
				    preview: previewImage1,
				    subtitle: subtitle1,
				    controls: controls1
				});
				$('#video_player_2').mhPlayer({
				    theme: theme2,
				    autoplay: autoplay2,
				    preview: previewImage2,
				    subtitle: subtitle2,
				    controls: controls2
				});
				$('#video_player_3').mhPlayer({
				    theme: theme3,
				    autoplay: autoplay3,
				    preview: previewImage3,
				    subtitle: subtitle3,
				    controls: controls3
				});
				$('#video_player_4').mhPlayer({
				    theme: theme4,
				    autoplay: autoplay4,
				    preview: previewImage4,
				    subtitle: subtitle4,
				    controls: controls4
				});
				$('#video_player_5').mhPlayer({
				    theme: theme5,
				    autoplay: autoplay5,
				    preview: previewImage5,
				    subtitle: subtitle5,
				    controls: controls5
				});
				$('#video_player_6').mhPlayer({
				    theme: theme6,
				    autoplay: autoplay6,
				    preview: previewImage6,
				    subtitle: subtitle6,
				    controls: controls6
				});
				$.mhPlayerSynch("#video_player_3", "#video_player_4");
				$.mhPlayerSynch("#video_player_5", "#video_player_6");
			    }(jQuery));
		       });
