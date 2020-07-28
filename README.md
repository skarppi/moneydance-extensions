
# Formula extension to Moneydance

Add formulas to Moneydance transaction reminders. Can be used to calculate e.g. payslip
with bonuses and taxes. This is an extension for Moneydance personal finance application.

To develop and build you'll need the Moneydance Developer Kit and 
documentation which is available at [developer site](https://infinitekind.com/developer).

## Requirements

* Use java version 11 at least
* Add `extadmin.jar` and `moneydance-dev.jar` from the devkit to the lib folder
* Copy `/Applications/Moneydance.app/Contents/Java/moneydance.jar` to `lib/moneydance-private.jar`.

## Usage

Open dialog from menu Extensions / Formulas.

![screenshot](https://github.com/skarppi/moneydance-formula/raw/master/screenshot.png "Screenshot")

Existing reminders can be included into Formula editor. Once added, transaction values can be
edited by altering A and B components and then committed (Record Transaction). Values can also be 
persisted for later use (Store parameters as default).

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
  