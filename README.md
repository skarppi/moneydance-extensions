
# Payslip extension to Moneydance

Calculate payslip transactions with taxes and bonuses. This is a extension to for the Moneydance personal finance application.

To develop and build you'll need the Moneydance Developer Kit and 
documentation which is available at [developer site](https://infinitekind.com/developer).

## Requirements

* Use java version 11 at least
* Add `extadmin.jar` and `moneydance-dev.jar` from the devkit to the lib folder
* Generate a key pair. This can be done by running "ant genkeys" from the "src"
  directory.  You will be prompted for a passphrase that is used to
  encrypt the private key file.  Your new keys will be stored in the
  priv_key and pub_key files.


## Usage

To compile and package the extension, run "ant payslip"
from the src directory.  After the extension is compiled and built,
you will be asked for the passphrase to your private key which will
be used to sign the extension and place the new extension file in
the dist directory with the name payslip.mxt.