# iciwi
Universal transportation ticket plugin.


# Development
1. Use JDK 17 & Gradle 8
2. Fork or clone this project locally
3. Wait for the project to boot up; Gradle may take some time
4. Start developing!

# Terminology used in this README
**Station**: A valid station defined in Iciwi's config.
**TOC**: Train Operating Company. Iciwi is built to handle multiple competing companies, not just a single nationalised railway network (although it could do that as well). TOCs are the entities defined in Iciwi that collect the final fares from railways.

# Usage
## Fare Gates
Fare gates are the contraptions to let players in and out of a transit network. They form the bulk of the signs on a server. To use them as a player, simply right-click on their sign or block, depending on the type of gate. However, for server admins, setting up may be a bit harder.

### Flags
Flags mostly determine which block the fare gate looks at for opening or closing. Here are the flags included with Iciwi:

- V: Validator. Makes the sign into a validator. (This mode makes the sign not cause blocks to update)
- S: Sideways sign. Place this sign on the right-hand side block before the fare gate.
- L: Lefty sign. Place this sign on the LEFT of the fare gate.
- D: Double fare gate. Use 2 blocks for the fare gate.
- R: Redstone activator. Activates the lever placed below the block that the sign is attached to.
- E: Eye-level sign. Place this sign 1 block above where you would normally place it.
- F: Trapdoor fare gate. Place a trapdoor 2 blocks above this sign to use, and right-click the trapdoor to use.

**By default (i.e. no flags), Iciwi's fare gate signs are placed on the block directly to the right of a fare gate.**

Flags may be combined in many combinations to give different results. For example, the flag combination `SLDE` creates a two-block-wide fare gate where its sign is placed on the left to the side, one block above where the fare gate is.

### Sign Types
Sign types tell the sign and the player using them (broadly) what to do. An entry sign allows a player to enter the network, an exit sign allows a player to exit the network, you get the idea. Like other plugins, the name of the sign type is denoted in [square brackets] as they are programmed to perform an action.

- [Entry]: Lets the player enter the network through the defined station.
- [Exit]: Lets the player exit the network from the defined station.
- [Member]: Only lets a player pass if they have a rail pass for the TOC that operates the sign's defined station.
- [Validator]: Both an [Entry] and [Exit] sign at once. This sign defaults to not opening anything.
- [Faregate]: Fare gate sign. This sign is equivalent to a [ValidatorF] sign. Kept for compatibility reasons.
- [Transfer]: Cuts a journey and starts a new one. Despite its name, in the code it does the opposite of what it says - it stops a transfer (explained below) from happening.
- [ClassChange]: Changes the ticket class for card users.

### Syntax
Each sign should follow the following syntax:
```
<Sign type><Flags>
<Station Name>
[anything]
[anything]
```
Mandatory fields are in <> while optional fields are in []. [anything] refers to literally anything.

### Using fare gates

There are two types of activation methods, explained below. In both cases, The plugin will check if the sign is valid and if the player fulfils all conditions to open the gate (things like whether the player has a valid ticket or card) before opening the gate. If there is no sign or if the sign is invalid, the second check will not happen and the handling will be passed back to the server; otherwise, the gate will continue to be closed.

- [Faregate] signs and signs with the F flag:
> Players should right-click on an openable block (i.e. fence gates and trapdoors) located two blocks above the sign.

- All other signs:
> Players should right-click on the sign itself.

## Ticket Machine
Ticket machines form up the other half of Iciwi. There are 4 types of ticket machines:

### Normal ticket machine
*Syntax:*
```
[Tickets]
<Station>
(anything)
(anything)
```
The normal ticket machine allows you to do everything you need with tickets. You can buy tickets for any journey from the station the ticket machine is located, or get an Iciwi card and perform related functions. Most stations should have at least one of these.

### Custom ticket machine
*Syntax:*
```
[CustomTickets]
<Station>
(anything)
(anything)
```
The custom ticket machine only allows you to buy single paper tickets. This machine should be found in either large stations with more rail lines or rural train stations to better assist in the purchase of tickets.

### Cards ticket machine
*Syntax:*
```
[Cards]
<Station>
(anything)
(anything)
```
The cards ticket machine lets players do operations on their Iciwi card only, and does not allow the purchase of paper tickets. It should be found in metro stations to encourage card use.

### Rail pass machine
*Syntax:*
```
[Passes]
<Station>
(anything)
(anything)
```
The rail pass machine allows you to only check and buy rail passes. While rail passes are currently required to be linked to a card, paper rail passes are planned in the future, and the rail pass ticket machine will be the only place where you can buy these paper passes.

## Transferring
Exiting one station and entering another within a certain time (changeable in config) counts as a transfer. Transferring will usually lead to lower fares as the two separate journeys made with a transfer in between are counted as a single journey.

However, this may lead to errors in pricing if a journey from a single system to another is made, especially if the second journey is lower-priced than the first. In that case, place a [Transfer] sign in the middle; it cuts the first journey and starts a new fare for the second. [Transfer] signs can also be placed in the paid area, in which they act like a [Exit] followed directly by an [Transfer] outside the gate line and then an [Entry].

# Admin usage

## fares.yml
This file lists all the fares.

Format:
```yml
FromStation:
  ToStation0:
    Class0: price
    Class1: price
    Class2: price
    Class3: price
  ToStation1:
    Class0: price
    Class1: price
    Class2: price
    Class3: price
...
```
You need to set a class as a default class to use when someone uses an Iciwi card to tap in/out. By default, the default class is `Second` (because metros aren't first class for obvious reasons)

## owners.yml
This file determines where the money goes when someone taps in/out.

`Aliases` Determines who to pay when someone uses the /coffers commands. This list is in the form `Operator: Username`. One operator can only be assigned one username, but one username can have many operators listed under them.

`Operators` Determines which **operator** owns which station. This list is in the form `Station: Operator`. One station can only be assigned one operator.

`Coffers` **You shouldn't touch this,** but this lists how much each operator has earned since the last `/coffers empty`. If a station does not have an operator, the money earned from that station goes to the operator `'null'`.

`RailPassPrices` Prices for rail passes. This list is in the form `Operator: Days: Price`, where Days refer to the number of days of free travel until expiry.

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

## What Iciwi CANNOT do
Iciwi only allows for fixed prices between stations. Time-discriminated and time-based prices are not available, and are not planned for the future. This is because Iciwi is built with mode integration in mind, i.e. commuters should be able to transfer between the metro and intercity trains seamlessly without worrying about paying double or paying for one over the other. 

The default Iciwi card also does not come with a fare cap for the same reason. Coding cards with fare caps opens a new can of worms that is calculating how much a single card has paid to a certain company within a certain time frame.

## Commands
Iciwi uses the Cloud Command Framework for its commands. Type /iciwi to see every command added.

## Dependencies
- BKCommonLib (you should already have this installed if you have TrainCarts installed.)
- Vault