# Know your friends

This app is gonna use Kotlin and Postgresql, and be deployed to Heroku.

Now, the app uses libraries such as
* [Ktor](https://github.com/Kotlin/ktor) as web web-service
* [Requery](https://github.com/requery/requery) for ORM functionality
* [Kotlin-logging](https://github.com/MicroUtils/kotlin-logging) for logging

## Running Locally

Make sure you have Java and Maven installed. Also, install the [Heroku Toolbelt](https://toolbelt.heroku.com/).

```sh
$ git clone https://github.com/edjacob25/doyouknowyourfriends
$ cd doyouknowyoutfriends
$ mvn clean install
$ heroku local:start
```

Your app should now be running on [localhost:5000](http://localhost:5000/).

To use the db, ensure you have a local `.env` file that reads something like this:

```
DB_NAME=dbname
DB_USER=username
DB_PASS=password
DB_PORT=5432
DB_SERVER=localhost
PORT=5000
DEBUG=true
```

## Deploying to Heroku

```sh
$ heroku create
$ git push heroku master
$ heroku open
```
