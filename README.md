# Moneydance Extension

Extensions in this repository are for [Moneydance personal finance application](https://moneydance.com).

To develop and build you'll need the Moneydance Developer Kit and
documentation which is available at [developer site](https://infinitekind.com/developer).

## Formula extension to Moneydance

Enhance Moneydance transaction reminders with Excel formulas. Can be used to calculate e.g. payslip
with variable bonuses and taxes, loans and interests with custom payment schedule, or any scheduled 
payment with variable equations.

### Usage

Open dialog from menu Extensions / Formulas.

![screenshot](https://github.com/skarppi/moneydance-extensions/raw/master/screenshot.png "Screenshot")

Existing reminders can be included into Formula editor. Once added, transaction values can be
edited by altering A * B + C components and then committed (```Record Transaction```). Source parameters can be 
persisted for later use (```Store parameters as default```).

Supported formulas
- Any python expression
- Custom variables, check all from tooltip for input ```?``` or ```dir()```
  - Reference any cell similar to Excel: A1, B1, C1, V1, A2,...
  - DAYS_IN_MONTH number of days in the month of the payment. Useful when calculating interests.
  - DAYS_IN_PREVIOUS_MONTH number of days in the preceding month of the payment. Useful when calculating interests.
  - BALANCE (current row), BALANCE1, BALANCE2,... the balance of the category.
- % sign can be used, e.g. 1% equals 0.01

## Crypto Importer

Import cryptocurrency transactions from [Binance](https://binance.com).

## Requirements

* Moneydance 2019 or later
* Use java version 11 at least
* Download and extract required libraries from the devkit to the lib folder.

```
./gradlew fetchLibs
```

## Running

By default, extensions are run without full Moneydance GUI for faster development cycle.
The account book is read only in this mode with multiple other limitations.

```
./gradlew :formula:run
./gradlew :crypto:run
```

Full Moneydance GUI can be started when testing full functionality. 

```
./gradlew deployUnsigned :run
```

## Deployments

The first time a key pair must be generated for signing. You will be prompted
for a passphrase that is used to encrypt the private key file and later
sign the extension on deployment.

```./gradlew genKeys```

To compile, package, and deploy the extension. The new signed extension file is copied to
Moneydance extension folder.

```./gradlew formula:deploy```
