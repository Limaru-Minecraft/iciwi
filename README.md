# iciwi
Iciwi Card Plugin for Limaru

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
## Transfer
Entry sign for entering paid zone used at out-of-station interchanges (OSIs)
```
[Transfer]
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
## Payment
One time payment, only accepts ICIWI cards, no paper tickets allowed
```
[Payment]
<payment made>
<player made payable to>
<anything>
```
# Commands
`/checkfare <from> <to>` Checks the fare from one station to another. The fares are taken from fares.json.
`/traindestroydelay` Reschedules the periodic train destroy.
`/ticketmachine <station>` Opens up a ticket machine for the specified station.
`/newdiscount <card serial number>` Redeems an existing card with the stated serial number.
