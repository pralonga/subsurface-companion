Subsurface Companion for Android
================================

Description
-----------
Utility tool for Subsurface diving log. Allows to upload dive positions.
For more informations about subsurface, see http://subsurface.hohndel.org/, or download it
from [Google Play Store][7].


How to build
------------
To build the application, you need [the Android SDK][1] and the followings:
* [Eclipse][2] with [Google Android Development Toolkit][3] ;
* [ActionBarSherlock][4], for compatibility reasons (optional if maven is used) ;
* [ORMLite][5], for easier DB management ;
* [Maven][6], with environment variable ANDROID_HOME set to your android SDK installation, and in your Path variable:
	* $ANDROID_HOME/tools:$ANDROID_HOME/platform-tools (Linux, Mac)
	* %ANDROID_HOME%\tools;%ANDROID_HOME%\platform-tools (Windows)
	* When executing "mvn install", a folder target and the application in it will be created
    * Use maven 3.0.1, if android-maven-plugin version is 3.6.0, or maven 3.1.1 if 3.8.2
* To use Maps in development, generate your own API key with your debug certificate (.android/debug.keystore), and
replace in AndroidManifest.xml the value of meta-data com.google.android.maps.v2.API_KEY. See [8] for complete instructions.


Licence
-------

	Subsurface for Android
	Copyright (C) 2012  Aurelien PRALONG
	
	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.



[1]: http://developer.android.com/sdk/index.html
[2]: http://www.eclipse.org/downloads/
[3]: http://developer.android.com/sdk/installing/installing-adt.html
[4]: http://actionbarsherlock.com/
[5]: http://ormlite.com/
[6]: http://maven.apache.org/download.cgi
[7]: https://play.google.com/store/apps/details?id=org.subsurface
[8]: https://developers.google.com/console/help/new/#generatingdevkeys
