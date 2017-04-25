

# Balances application in Scala using the Play Framework

This application was developed using the Play Framework, SBT and Scala. Its aim is to simulate a Banking application that deals with balances

## Running

```
sbt run
```

And then go to http://localhost:9000 to see the running web application.

## Testing
```
sbt test
```
The console shall give the results. The tests are specified in the file BalancesSpec at the "test" folder.
## Controllers
```
BalancesController.Scala
BalancesDao.Scala
```
The first file defines all basic operations. Request details are given below

The second file abstracts the data features. I decided not to use any databases but to use only data structures, nevertheless it should be easy to add DB support using the abstraction. It's injected on the Controller using Guice, it's created as a Singleton on Module.Scala

Adds deposit
```
http://localhost:9000/operations/deposit 
{"accountNumber":8312,"value":20,"date":"2017-04-02","description":"Test deposit"}
```
Adds withdrawal
```
http://localhost:9000/operations/withdrawal 
{"accountNumber":8312,"value":15.2,"date":"2017-04-5","description":"Test withdrawal"}
```
Adds purchase
```
http://localhost:9000/operations/purchase
{"accountNumber":8312,"value":10.2,"date":"2017-04-12","description":"Test purchase"}
```
Gets the statement (int, string, string)
```
http://localhost:9000/statement/8312/2017-04-01/2017-04-30
```
Gets debt (int)
```
http://localhost:9000/debt/8312
```


## Filters
No filters were used

