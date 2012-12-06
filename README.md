# android-wireless-device-logger

http://pokutuna.github.com/android-wireless-device-logger/


## download



## build
__Don't mind this part if you install .apk file__

This app needs Dropbox application keys.  
Make a xml file including app keys like this and put it to `src/main/res/values/secrets.xml`.

```xml
<resources>
  <string name="DropboxAppKey">***************</string>
  <string name="DropboxAppSecret">***************</string>
</resources>
```

And,

```
$ sbt
> android:start-device
```


## using library
this software id depending following libs.

- [Dropbox SDK for Android](https://www.dropbox.com/developers/reference/sdk)
- [jgilfelt / android-viewbadger](https://github.com/jgilfelt/android-viewbadger)
