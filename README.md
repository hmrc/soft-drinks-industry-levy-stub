# Soft Drinks Industry Levy Stub

[ ![Download](https://api.bintray.com/packages/hmrc/releases/soft-drinks-industry-levy-stub/images/download.svg) ](https://bintray.com/hmrc/releases/soft-drinks-industry-levy-stub/_latestVersion)

## About
The Soft Drinks Industry Levy (SDIL) digital service is split into a number of different microservices all serving specific functions which are listed below:

Liability tool - Standalone frontend service that is used to check a company's liability in regards to the levy.

Frontend - The main frontend for the service which includes the pages for registration.

Backend - The service that the frontend uses to call HOD APIs to retrieve and send information relating to business information and subscribing to the levy.

Stub - Microservice that is used to mimic the DES APIs when running services locally or in the development and staging environments.

This is the stub.

For details about the sugar tax see [the GOV.UK guidance](https://www.gov.uk/guidance/soft-drinks-industry-levy)

## APIs

#### POST        /soft-drinks/subscription/:idType/:idNumber  
Stub of the ETMP create subscription endpoint. idType must be UTR.
      
#### GET         /soft-drinks/subscription/details/:idType/:idNumber
Stub of the ETMP retrieve subscription endpoint. Returns an SDIL subscription, or 404 if no record exists. Accepts UTRs or SDIL number.

#### POST        /registration/organisation/utr/:utr
Stub of the ROSM Business Partner Record API. Returns the BPR, or 404 if no record exists.

## Running from source
Clone the repository using SSH:

`git@github.com:hmrc/soft-drinks-industry-levy-stub.git`

If you need to setup SSH, see [the github guide to setting up SSH](https://help.github.com/articles/adding-a-new-ssh-key-to-your-github-account/)

Run the code from source using 

`sbt run`

The APIs are then accessible at `http://localhost:8702`

## Running through service manager

Run the following command in a terminal: `nano /home/<USER>/.sbt/.credentials`

See the output and ensure it is populated with the following details:

```
realm=Sonatype Nexus Repository Manager
host=NEXUS URL
user=USERNAME
password=PASSWORD
```

*You need to be on the VPN*

Ensure your service manager config is up to date, and run the following command:

`sm --start SDIL_ALL -f`

This will start all the required services

## Generating a UTR

From auth login stub, pass in a UTR of the form `XXXXXABCDE`.

### Unregistered UTR - to reach registration journey

Pass in `ABCDE` as `00000`

### A - Deregistration Date

| Value for A | Deregistration Date         |
|-------------|-----------------------------|
| 7           | Between 2.5 and 3 years ago |
| 8           | Between 1.5 and 2 years ago |
| 9           | Between 0.5 and 1 years ago |
| 6 or under  | None                        |

### BC - Activity Prod Type & Activity

| Value for B | isImporter | isContractPacker |
|-------------|------------|------------------|
| 0           | false      | false            |
| 1           | true       | false            |
| 2           | false      | true             |
| 3           | true       | true             |

| Value for C | producesOwnBrands | isCopackee    | isLarge       |
|-------------|-------------------|---------------|---------------|
| 0           | false             | false         | false         |
| 1           | false             | true          | false         |
| 2           | true              | false         | false         |
| 3           | true              | true          | false         |
| 4           | false             | true          | true          |
| 5           | true              | false         | true          |
| 6           | true              | true          | true          |

### D - Years of Liability

Liability date will be generated between `D + 0.5` and `D + 1` years ago  

### E - Production Sites and Warehouse Sites

| Value for E  | Production Sites | Warehouse Sites |
|--------------|------------------|-----------------|
| 0            | 0                | 0               |
| 1            | 1                | 0               |
| 2            | 2                | 0               |
| 3            | 0                | 1               |
| 4            | 1                | 1               |
| 5            | 2                | 1               |
| 6            | 0                | 2               |
| 7            | 1                | 2               |
| 8            | 2                | 2               |

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")