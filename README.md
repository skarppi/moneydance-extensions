
# Formula extension to Moneydance

Enhance Moneydance transaction reminders with Excel formulas. Can be used to calculate e.g. payslip
with variable bonuses and taxes, loans and interests with custom payment schedule, or any variable payment. 

This is an extension for [Moneydance personal finance application](https://moneydance.com).

To develop and build you'll need the Moneydance Developer Kit and 
documentation which is available at [developer site](https://infinitekind.com/developer).

## Usage

Open dialog from menu Extensions / Formulas.

![screenshot](https://github.com/skarppi/moneydance-formula/raw/master/screenshot.png "Screenshot")

Existing reminders can be included into Formula editor. Once added, transaction values can be
edited by altering A * B components and then committed (Record Transaction). Source parameters can also be 
persisted for later use (Store parameters as default).

Supported formulas
- Any python expression
- Custom variables, check all by typing ```?``` or ```dir()```
  - Reference any cell similar to Excel: A1, B1, A2,...
  - DAYS_IN_MONTH number of days in the current month. Useful when calculating interests.
  - BALANCE the balance of the selected category. Useful when calculating interests.
- % sign can be used, e.g. 1% equals 0.01

## Requirements

* Moneydance 2020 or 2019
* Use java version 11 at least
* Add `extadmin.jar` and `moneydance-dev.jar` from the devkit to the lib folder
* Copy `/Applications/Moneydance.app/Contents/Java/moneydance.jar` to `lib/moneydance-private.jar`.

## Deployments

Alternatively use MockRunner to run the extension only in IDE.

* Generate a key pair. This can be done by running "ant genkeys" from the "src"
  directory.  You will be prompted for a passphrase that is used to
  encrypt the private key file.  Your new keys will be stored in the
  priv_key and pub_key files.
* To compile and package the extension, run "ant formula"
  from the src directory.  After the extension is compiled and built,
  you will be asked for the passphrase to your private key which will
  be used to sign the extension and place the new extension file in
  the dist directory with the name formula.mxt.
  