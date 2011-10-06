/*
The Matterhorn2Go Project
Copyright (C) 2011  University of Osnabrück; Part of the Opencast Matterhorn Project

This project is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 
USA 
*/
package business.datahandler
{
	import events.VideosLoadedEvent;
	
	import flash.events.EventDispatcher;
	
	import mx.collections.XMLListCollection;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	
	import spark.events.IndexChangeEvent;
	
	public class DataHandler extends EventDispatcher 
	{
		private var serviceObj:HTTPService;
		private var matterhornURL:String;
		
		private var xmlVideos:XMLListCollection;
		private var cDate:Object;
		
		private var total:int;
		private var limit:int;
		private var offset:int;
		private var sText:String = "";
		private var oValue:String = "0";
		
		static private var instance:DataHandler;
		
		public function DataHandler()
		{			
			this.serviceObj = new HTTPService();
		}
				
		static public function getInstance():DataHandler 
		{
			if (instance == null) instance = new DataHandler();

			return instance;
		}
		
		// The initialisation function
		public function init():void
		{
			this.matterhornURL = URLClass.getInstance().getURL()+'/search/episode.xml';
			serviceObj.resultFormat = 'e4x';
			serviceObj.method = 'GET';
			serviceObj.useProxy = false;
			serviceObj.addEventListener(ResultEvent.RESULT, processResult);
			serviceObj.url = matterhornURL+'?q='+sText+'&offset='+oValue;	
			serviceObj.send();
		}
	
		// The result processing function
		public function processResult(response:ResultEvent):void
		{
			var XMLResults:XML = response.result as XML;
			total = XMLResults.@total;
			limit = XMLResults.@limit;
			offset = XMLResults.@offset;
			
			xmlVideos = new XMLListCollection(XMLResults.result.mediapackage);

			var videoLoaded:VideosLoadedEvent = new VideosLoadedEvent(VideosLoadedEvent.VIDEOSLOADED);
			dispatchEvent(videoLoaded);	
		}
	
		// search videos
		public function search(searchText:String, offset_value:String):void
		{
			this.sText = searchText;
			this.oValue = offset_value;
			
			var url:String = matterhornURL;
			var searchurl:String = url+'?q='+sText+'&offset='+oValue;
			serviceObj.url = searchurl;
			serviceObj.send();
			var videoLoaded:VideosLoadedEvent = new VideosLoadedEvent(VideosLoadedEvent.VIDEOSLOADED);
			dispatchEvent(videoLoaded);	
		}
		
		public function getXMLListCollection():XMLListCollection
		{
			return xmlVideos;
		}
		
		public function getTotal():int
		{
			return total;
		}
		
		public function getLimit():int
		{
			return limit;
		}
		
		public function getOffset():int
		{
			return offset;
		}
		
		public function getText():String
		{
			return sText;
		}
	}
}