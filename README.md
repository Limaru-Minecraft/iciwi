# iciwi
Universal transportation ticket plugin.

[![Java CI with Gradle](https://github.com/Mineshafter61/iciwi/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/Mineshafter61/iciwi/actions/workflows/gradle-publish.yml)

# Development
1. Use JDK 16 or 17 & Gradle
2. Download and run [BuildTools](https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar)
3. Fork or clone this project locally
4. Start developing!

# Usage
## Entry and Exit
Normal entry sign for entering paid zone
```
[<Entry:Exit><V:S:L:D:R:E>]
<station name>
<anything>
<anything>
```
### First line parameters
1. Entry/Exit: Determines the action of the sign.
2. V: Validator. Makes the sign into a validator. (This mode makes the sign not cause blocks to update)
3. S: Sideways sign. Place this sign on the right-hand side block before the fare gate.
4. L: Lefty sign. Place this sign on the LEFT of the fare gate.
5. D: Double fare gate. Use 2 blocks for the fare gate.
6. R: Redstone activator. Activates the lever placed below the block that the sign is attached to.
7. E: Eye-level sign. Place this sign 1 block above where you would normally place it.

## Harlon-style fare gate
A Harlon-style fare gate. Used for BOTH entry and exit. Right click on the trapdoor to open.
### Placement
```
(Iron trapdoor)
(Block: Double slab for entry, full block for exit)
(Sign: explained below)
```
### Sign
```
[Faregate]
<station name...>
<...station name...>
<...station name>
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

`Check Fares` View the auto-generated farecharts.

## Transferring
Exiting one station and entering another within a certain time counts as a transfer.

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
