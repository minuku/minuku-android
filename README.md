# Minuku

Minuku is an Android app designed for crowdsensing and researching people’ daily life behaviors. It can be configured to collect various types of contextual data (e.g. sensor, location, activity, phone status) on Android phones, and monitor situations based on the contextual data. It can implement diary or an ESM, and it prompt participants with questionnaires based on a configured schedule or context (i.e. Context-Triggered ESM). It also has an interface for users to review and annotate contextual data collected by Minuku.

## Requirement
* Android Studio

## Configuration

#### DataRecord 
/minuku/model
* Accessibility
* ActivityRecognition
* AppUsage
* Battery
* Connectivity
* Location
* Ringer
* Sensor
* Telephony
* Transportation

#### Data Access Object (DAO)
/minuku/dao
* RoomDatabase
  Room provides an abstraction layer over SQLite to allow fluent database access while harnessing the full power of SQLite.

#### Stream / StreamGenerator
* A set of digital signals used for different kinds of data transmission


## Usage
1. Directly clone Minuku project
```
git clone "THIS_PROJECT"
```
2. Replace ```app``` folder with your own application.


## Contact
Feel free to [contact us](jyunyan.lu@gmail.com) if there's any problem.

## Links

Reference from [Android Developer Guide](https://developer.android.com/guide/) , including [Room](http://scrapy-cookbook.readthedocs.io/zh_CN/latest/scrapy-12.htmlhttps://developer.android.com/training/data-storage/room/)
