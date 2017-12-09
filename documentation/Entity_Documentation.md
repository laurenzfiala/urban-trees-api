# Entity Documentation

for Sparkling Science project "Urban Trees as climate messengers"

See http://stadt-baum-klima.sbg.ac.at/ for more information.

## Trees

After trees are chosen by the project team, they are entered into the database ([urban-trees-db](https://github.com/laurenzfiala/urban-trees-db)). City, street and exact geographical location are stored to identify the trees. One project partner school has multiple trees to observe.

When a tree is set up, it is also check for all information found in the database tables beginning with `tree_*`.

##Beacons

"Beacons" are Bluetooth-enabled devices which gather data about their surroundings. In the context of this project, beacons are used to gather temperature and humidity information. These beacons are put onto trees and store their data in a set interval. When someone walks by with the appropriate app open, the app and the beacons identify & authenticate each other and the app places an HTTP call to the [urban-trees-api](https://github.com/laurenzfiala/urban-trees-api) which in turn, inserts the new data in the [urban-trees-db](https://github.com/laurenzfiala/urban-trees-db). The app may insert multiple datasets at once (e.g. when it's been a long time since someone checked the tree with the application).

## Phenology & Physiognomy data

These datasets are not gathered and stored automatically. They are dependent on project participants, which will observe the trees and use a web interface to enter the data (a specific website on [urban-trees-web](https://github.com/laurenzfiala/urban-trees-web)).