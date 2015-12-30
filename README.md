# andors-trail
Andor's Trail

In this fork I'm adding more options for monster AI 
(passive become aggressive when attacked, monsters can flee when HP below threshold).

I'm also working on adding ranged-attacks, which are intitially only enabled through debug-buttons.

#Commit#  1
Code needs to be cleaned but works fine (considering the missing implementations)
1. Debug options: Added ranged & teleport button
2. InputController: If ranged or teleport enabled, take position of original touched-tile (and not relative to current_tile).
This allows for distant tiles to be selected for attack/teleport.
3. CombatController: Monsters follow player when aroused (is a customizable property)
+ incomplete support for fleeing when attacked/ below HP threshold.
4. MonsterType: Added messy enum table for properties (to be integrated later into monster data).
e.g. "flees-for-HP", "flees-directly", "doesn't flee", "does/doesn't engage when attacked with range"

To be done: 
- Standard monster properties for new data
- Calculate distance to ranged-target/teleport-location
- Make fleeing more user-friendly (auto, like attacking, instead of current re-clicks of button)
- Monsters flee by going opposite direction?
 (Maybe djksitra maps e.g. brogue)
 - Issuing ranged-attacks without going into combat screen? Need to better understand the signals sent between screens.
