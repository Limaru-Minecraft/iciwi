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
`/redeemcard <card serial number>` Redeems a card
`/coffers empty <company name>` Withdraws the money stored in your company's coffers.
`/coffers view [company name]` View the money stored in your company's coffers.

# Transferring
Exiting one station and entering another within 5 min counts as a transfer. (not implemented yet)
