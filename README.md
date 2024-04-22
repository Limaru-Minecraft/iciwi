# iciwi
Universal transportation ticket plugin.


# Development
1. Use JDK 17 & Gradle 8
2. Fork or clone this project locally
3. Checkout "alpha" branch
4. Start developing!

# Terminology used in this README
**Station**: A valid station defined in Iciwi's config.<br>
**TOC**: Train Operating Company. Iciwi is built to handle multiple competing companies, not just a single nationalised railway network (although it could do that as well). TOCs are the entities defined in Iciwi that collect the final fares from railways.

# Usage
## Fare Gates
Fare gates are the contraptions to let players in and out of a transit network. They form the bulk of the signs on a server. To use them as a player, simply right-click on their sign or block, depending on the type of gate. However, for server admins, setting up may be a bit harder.

### Flags
Flags mostly determine which block the fare gate looks at for opening or closing. Here are the flags included with Iciwi:

- **V**: Validator. Makes the sign into a validator. (This mode makes the sign not cause blocks to update)
- **S**: Sideways sign. Place this sign on the right-hand side block before the fare gate.
- **L**: Lefty sign. Place this sign on the LEFT of the fare gate.
- **D**: Double fare gate. Use 2 blocks for the fare gate.
- **R**: Redstone activator. Activates the lever placed below the block that the sign is attached to.
- **E**: Eye-level sign. Place this sign 1 block above where you would normally place it.
- **F**: Trapdoor fare gate. Place a trapdoor 2 blocks above this sign to use, and right-click the trapdoor to use.

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

There are two types of activation methods, explained below. In both cases, the plugin will check if the sign is valid and if the player fulfils all conditions to open the gate (things like whether the player has a valid ticket or card) before opening the gate. If there is no sign or if the sign is invalid, the second check will not happen and the handling will be passed back to the server; otherwise, the gate will continue to be closed.

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
The rail pass machine allows you to only check and buy rail passes. While most rail passes can be bought in paper form, rail passes preceded by an underscore can only be applied to an Iciwi Card.

## Transferring
Exiting one station and entering another within a certain time (changeable in config) counts as a transfer. Transferring will usually lead to lower fares as the two separate journeys made with a transfer in between are counted as a single journey.

However, this may lead to errors in pricing if a journey from a single system to another is made, as no fare between the two endpoints is defined. In that case, the transfer will be counted as two separate journeys.

At stations where both systems may share a single platform, `[Transfer]` signs can be placed in the paid area. This sign breaks the journey into two separate journeys, allowing for players to be charged correctly.

# Config files

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
```
You need to set a class as a default class to use when someone uses an Iciwi Card to tap in/out. By default, the default class is `_Second` (because metros aren't first class for obvious reasons)

**Note that classes preceded by an underscore are only accessible using an Iciwi Card.**

## owners.yml
This file determines where the money goes when someone taps in/out.

`Aliases`<br>
This section determines which player is the owner of which company.<br>
The owner of a company is paid whenever a player uses their services.<br>
A single section in this section will look as follows:
```yml
OPERATOR_NAME: PLAYER_NAME
```
<hr>

`Operators`<br>
This section assigns stations to companies. You will need to fill this up even if there is only 1 company on your server.<br>
Stations not belonging to any company will belong to the "null" company. (The station name refers to the name in `fares.yml`.)<br>
A single section in this section will look as follows:
```yml
STATION_NAME:
- OPERATOR_NAME_0
- OPERATOR_NAME_1
```
<hr>

`RailPasses`<br>
This section describes rail passes that give discounted travel for a certain duration to a player between a given operator's stations.<br>
A single section in this section will look as follows:
```yml
NAME_OF_RAIL_PASS:
  operator: OPERATOR_NAME
  duration: VALID_DURATION
  price: PRICE_OF_RAIL_PASS
  percentage: FRACTION_CHARGED (if it's 1/3 off, put 0.67)
```
<hr>

`TicketType`<br>
(FUTURE) Buttons to show when a player opens a [Tickets] ticket machine.

### Default file:
```yml
# This section determines which player is the owner of which company.
# The owner of a company is paid whenever a player uses their services.
# The format is <company>: <owner's username>
Aliases:
  ExampleOperator: Mineshafter61
  ExampleOperator1: Mineshafter62

# This section describes rail passes that give discounted travel for a certain duration to a player between a given operator's stations.
# The format is in:
# Name of rail pass:
#   operator
#   duration
#   price
#   fraction payable
RailPasses:
  '7 days half price':
    operator: ExampleOperator
    duration: '7:00:00:00'
    price: 32.5
    percentage: 0.5
  '30 days free journeys':
    operator: ExampleOperator
    duration: '30:00:00:00'
    price: 109.9
    percentage: 0.0
  '8 days free journeys':
    operator: ExampleOperator1
    duration: '8:00:00:00'
    price: 32.5
    percentage: 0.0
  '35 days half price':
    operator: ExampleOperator1
    duration: '7:00:00:00'
    price: 60.0
    percentage: 0.5

Operators:
  ExampleStation:
  - ExampleOperator
  - ExampleOperator1
  ExampleStation1:
  - ExampleOperator2

#TicketType:
  #FUTURE SECTION DO NOT USE
```

## What Iciwi CANNOT do
Iciwi only allows for fixed prices between stations. Time-discriminated and time-based prices are not available, and are not planned for the future. This is because Iciwi is built with mode integration in mind, i.e. commuters should be able to transfer between the metro and intercity trains seamlessly without worrying about paying double or paying for one over the other.

# Commands
Iciwi uses the Cloud Command Framework for its commands. Type /iciwi to see every command added.

Commands can be used to modify most parts of Iciwi's config files.

### Main config
| Command | Description | Permission |
| --- | --- | --- |
| `iciwi reload` | Reloads all configuration files | `iciwi.reload` |
| `iciwi penalty <amount>` | Sets the penalty penalty given to fare evaders | `iciwi.penalty` |
| `iciwi deposit <amount>` | Sets the deposit paid when buying a new card | `iciwi.deposit` |
| `iciwi addpricelist <amount>` | Adds an option to the choices of card values | `iciwi.addpricelist` |
| `iciwi removepricelist <amount>` | Adds an option to the choices of card values | `iciwi.removepricelist` |
| `iciwi maxtransfertime <amount>` | Sets the maximum time allowed for an out-of-station transfer to happen. | `iciwi.maxtransfertime` |
| `iciwi gateclosedelay <amount>` | Sets the duration whereby fare gates open. | `iciwi.gateclosedelay` |
| `iciwi closeafterpass <amount>` | Sets the duration for which the gates are still open after a player walks through. | `iciwi.closeafterpass` |
| `iciwi defaultfareClass <fareClassname>` | Sets the fare fareClass used by default when card payment is used. | `iciwi.defaultfareClass` |

### Owners
| Command | Description | Permission |
| --- | --- | --- |
| `iciwi owners alias set <company> <username>` | Sets the revenue collector for a company. | `iciwi.owners.alias` |
| `iciwi owners alias unset <company>` | Removes the revenue collector for a company. | `iciwi.owners.alias` |
| `iciwi owners operator <station> add <company>` | Adds an owning company to a station. | `iciwi.owners.operator` |
| `iciwi owners operator <station> set <company>` | Sets the owning company of a station. | `iciwi.owners.operator` |
| `iciwi owners operator <station> delete` | Removes all owning companies of a station. | `iciwi.owners.operator` |
| `iciwi owners railpass <name> operator <company>` | Sets the rail company that owns the given railpass. | `iciwi.owners.railpass` |
| `iciwi owners railpass <name> duration <duration>` | Sets the duration that the given railpass is active. | `iciwi.owners.railpass` |
| `iciwi owners railpass <name> price <amount>` | Sets the price of the given railpass. | `iciwi.owners.railpass` |
| `iciwi owners railpass <name> percentage <paidpercentage>` | Sets the percentage paid by the card holder when they use the railpass. | `iciwi.owners.railpass` |
| `iciwi owners railpass <name> delete` | Deletes a railpass. | `iciwi.owners.railpass` |
| `iciwi owners railpass <name> delete` | Deletes a railpass. | `iciwi.owners.railpass` |

### Fares
| Command | Description | Permission |
| --- | --- | --- |
| `iciwi fares set <start> <end> <fareClass> <price>` | Creates a new fare. | `iciwi.fares.set` |
| `iciwi fares check <start> [end] [fareClass]` | Either checks for all destinations from a station, all the fare classes for a journey or the fare between two stations for a fare class. | `iciwi.fares.check` |
| `iciwi fares unset <start> <end> <fareClass>` | Deletes a fare. | `iciwi.fares.unset` |
| `iciwi fares deletejourney <start> <end>` | Deletes all fares between a start and end point. | `iciwi.fares.deletejourney` |
| `iciwi fares deletestation <start>` | Removes a station and all its associated fares from the data. | `iciwi.fares.deletestation` |

## Dependencies
- BKCommonLib (you should already have this installed if you have TrainCarts installed.)
- Vault
