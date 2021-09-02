# iciwi
Iciwi Card Plugin for Limaru

[![Java CI with Maven](https://github.com/Mineshafter61/iciwi/actions/workflows/IciwiBuild.yml/badge.svg)](https://github.com/Mineshafter61/iciwi/actions/workflows/IciwiBuild.yml)

# Usage
## Entry
Normal entry sign for entering paid zone
```
[Entry]
<station name>
<anything>
<anything>
```
## Exit
Normal exit sign for exiting paid zone
```
[Exit]
<station name>
<anything>
<anything>
```
## Ticket Machine
A ticket machine
```
[Tickets]
<station name>
<anything>
<anything>
```
# Commands
`/checkfare <from> <to>` Checks the fare from one station to another. The fares are taken from fares.json.

`/ticketmachine <station>` Opens up a ticket machine for the specified station.

`/newdiscount <card serial number> <company name> <days till expiry>` Gives the card a rail pass (free journey from/to one company's stations)

`/redeemcard <card serial number>` Redeems a card from the database.

`/coffers empty <company name>` Withdraws the money stored in your company's coffers.

`/coffers view [company name]` View the money stored in your company's coffers.

## Ticket Machine Usage
`New Single Journey Ticket` Key in the price of the ticket using the keypad. This can be changed later to a higher price at another ticket machine (select `Adjust Fares`).

`Iciwi Card Operations` Buy, top up, add a rail pass to, and refund cards. If you do not have a card in your inventory, this button automatically redirects you to the new card page.

`Check Fares` FOR LIMARU ONLY: View the auto-generated farecharts. (Auto-generated links may be implemented later.)

## Transferring
Exiting one station and entering another within 5 min counts as a transfer. (not implemented yet)

# Admin usage

## fares.json
This file lists all the fares. The format is `{"StartStation": {"EndStation": fare}}`. USE CAMELCASE FOR THIS FILE.

This file reloads automatically when someone uses a fare gate, so doing a reload is not required. This makes making a 3rd-party plugin to implement time-based fares easy.

## owners.yml
This file determines where money goes to when someone taps in/out.

`Aliases` Determines who to pay when someone uses the /coffers commands. This list is in the form `Operator: Username`. One operator can only be assigned one username, but one username can have many operators listed under them.

`Operators` Determines which **operator** owns which station. This list is in the form `Station: Operator`. One station can only be assigned one operator.

`Coffers` **You shouldn't touch this,** but this lists how much each operator has earned since the last `/coffers empty`. If a station does not have an operator, the money earned from that station goes to the operator `'null'`.

`RailPassPrices` Prices for rail passes. This list in the form `Operator: Days: Price`, where Days refers to the number of days of free travel until expiry.

Sample `owners.yml`:
```yml
Aliases:
  ExampleOperator: ExampleUsername
Operators:
  ExampleStation: ExampleOperator
Coffers:
  ExampleOperator: 0.0
  'null': 4.22
RailPassPrices:
  ExampleOperator:
    '7': 25.0
    '30': 100.0
```
