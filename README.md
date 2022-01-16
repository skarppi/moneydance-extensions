
# Formula extension to Moneydance

Enhance Moneydance transaction reminders with Excel formulas. Can be used to calculate e.g. payslip
with variable bonuses and taxes, loans and interests with custom payment schedule, or any scheduled 
payment with variable equations.

This is an extension for [Moneydance personal finance application](https://moneydance.com).

To develop and build you'll need the Moneydance Developer Kit and 
documentation which is available at [developer site](https://infinitekind.com/developer).

## Usage

Open dialog from menu Extensions / Formulas.

![screenshot](https://github.com/skarppi/moneydance-formula/raw/master/screenshot.png "Screenshot")

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

## Requirements

* Moneydance 2019 or later
* Use java version 11 at least
* Add `extadmin.jar` and `moneydance-dev.jar` from the devkit to the lib folder
* Copy `/Applications/Moneydance.app/Contents/Java/moneydance.jar` to `lib/moneydance-private.jar`.

## Running

By default, the extension is run without full Moneydance GUI for faster development cycle.
The account book is read only in this mode with multiple other limitations.

```
./gradlew run
```

Full Moneydance GUI can be started when testing full functionality. 

```
./gradlew runFull
```

## Deployments

The first time a key pair must be generated for signing. This can be done by running ```./gradlew genKeys```.
You will be prompted for a passphrase that is used to encrypt the private key file.

To compile, package, and deploy the extension, run ```./gradlew deploy```. 
You will be asked for the passphrase to your private key which will
be used to sign the extension. The new signed extension file is copied to 
Moneydance extension folder.
  