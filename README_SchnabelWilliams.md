# Lab 08: Battle Bots

Michael Schnabel    michael.schnabel@nps.edu
Connor Williams     connor.williams@nps.edu

## Overview

This lab required students to develop an autonomous Robocode tank to battle 
1v1 against sample bots and in a free-for-all melee fight against all the other
student bots. We implemented VanJeckylson, which incorporates highly dynamic 
movement, improved targeting, and variable tactics for 1v1 and melee battles.

## Methodology and Approach

### Robot Algorithmic Design

We approached designing the bot by initially working on two separate bots independently,
then merging the best concepts from both into a final product. We tested our bots against
the sample bots repeatedly until we got to a point where both were successful the vast
majority of the time. We primarily used the online robocode documentation and wiki as a
starting point for our algorithms and code, then adapted them as we saw fit. For testing,
we ran our bots 1v1 against all the sample bots, as well as in a melee against all the
sample bots at once. Additionally, we loaded both of our bots simultaneously under different
names and competed them 1v1 and in a melee to see whose tactics/algorithms were working best.
 
The primary surviving algorithm concepts in our bot are dynamic targeting, a hybrid movement
system, enemy tracking, and variable tactics for the different types of engagements. For dynamic targeting,
we estimate future enemy position using linear prediction based on bullet travel time, and enemy direction / speed. We vary the firepower based on these factors and enemy distance so the bot fires more aggressively at closer targets and weaker shots for lower confidence targets.
 
The movement system is the most complex part of the code and is primarily how we implement different tactics
for 1v1 vs melee. We created a hybrid movement system using "antigravity" (which uses
repulsive forces that "push" the bot away from walls and enemies) and a more fixed,
aggressive movement system that strafes the enemy and incorporates some random direction
switches.
 
When there are more than 2 enemy bots in the arena, VanJeckylson biases more
towards conservative antigravity-style movement in order to preserve itself longer. When
the field drops down to 2 or less enemy, VanJeckylson biases more towards the aggressive,
deterministic movement. Neither movement system is entirely disregarded at any point,
however, allowing our bot to keep the best of both types but adjust to its circumstances.
 
The bot also tracks recently scanned enemies in an array. The antigravity system uses the array to calculate repulsive forces, which prevents VanJeckylson from driving directly into crowded areas.

### Object Oriented Programming

To understand the RoboCode library, we utilized the README and sample bots some, but our primary source
was the Robocode.dev site and the Robocode wiki. The README and sample bots got us started with initial
iterations of the bots and allowed us to get our feet under us with respect to the built in library
methods and event handlers. The dev site and wiki are what facilitated our learning about the tactics/
strategy for writing and structuring the bot, as well as some best practices when implementing various
pieces of the bot (such as dynamic shot power level, movement techniques, and melee vs 1v1 tactics).
Java being highly object oriented made this a breeze, specifically in that it gave us (via inheritance)
an incredibly rich and well built out set of functions that were cleanly documented and easily 
implemented. It let us essentially isolate all the behaviors we wanted to implement as methods or as
modifications to existing event handlers then simply create a Run() function to actually call them during a
turn. In short, the object oriented nature gave us the ability to abstract the desired behaviors into
methods/event handlers then easily put them together in a Run() method. In addition, the abstraction
allowed us to easily iterate and debug specific behaviors, whereas a more monolithic type of approach
would have been less easily readable and comprehendable for us as developers. 

## Generative AI and LLM Consultation

We used Ai for some debugging and code generation (e.g. update this method to incorporate feature X), but 
that was the extent. Starting with a template of MyFirstBot.java, we wrote the methods and event handlers
ourselves then turned to Ai for debugging help if needed. Additionally, on the conceptual side, we used Ai
to help clarify things about Robocode API usage and to sanity check our responses/thinking for the README.

## Challenges

The object oriented paradigm felt in no way like a hindrance to implementing our chosen algorithm. It 
almost exclusively made it easier because it allowed us to encapsulate and isolate our desired behaviors
then focus on improving those one at a time versus wrestling with some large, confusing IF/ELSE tree.
Additionally, inheritance helped a ton in that it gave us a ready made API that handled the vast majority
of complex tasks that the game involved. This would have been a considerably harder project if things like
'onScannedBot()' and 'getEnemyCount()' didn't exist and we had to implement them ourselves. 
Some of the math parts were a little ugly and hard to read (e.g. "Math.sin(Math.toRadians(e.getDirection()))"),
but for the most part this project lent itself very nicely to object orientation. Especially coming
from a background in primarily Python, none of this lab felt particularly awkward or forced. If anything it 
was exactly how we were used to accomplishing tasks like this.

## Critical Thinking

**Object Oriented Programming and modularization**

1. The classes and methods made it simple to divide the robot into behaviors. Both of us could work on 
any of the behaviors at a time, for example, with Connor developing movement logic while Mike worked on 
targeting. Keeping the rest of the behaviors the same made iterating simpler in that we both would keep 
the same "starting point" while making our own changes to see what worked. Making a change to 
`computeVJMovement()` would not affect `pickFirePower()`.
 
It would have been more difficult to achieve this in a purely imperative approach because the procedural 
steps for the robot are not clear when designing its behavior. In this case, without explicit methods and 
attributes, making a change to movement patterns would require careful design to avoid unintended 
consequences to targeting.
 
2. Inheriting from the `Bot` class gave us a clear starting point for our code. It allowed us to abstract 
away the low-level Robocode-isms and focus on the hooks to begin our implementation. `run()`, `onScannedBot()`, 
and `onHitWall()`, for example, make it clear to the developer how our bot interacts in the battle. This gave 
a simple, event-driven skeleton for developing our bot.
 
A couple challenges did come from using the API. When designing the antigravity movement pattern, we could 
have imported from `robocode.util.Utils` and the implementation would have been far easier. However, the API 
we were working with did not allow this import. Another complexity of using the API was that the radar and 
gun movement are tied together. We had to design scanning and aiming around turning the gun. It would be 
beneficial to be able to scan without moving the gun and only moving the gun on request.
 
3. The "contract" concept of OOP made collaborative work a lot simpler than dealing with pure imperative 
programming. Each method had a purpose that should not interfere with other methods. As mentioned in our 
algorithm development, keeping method responsibilities separate ensured we could each develop on our own 
and test changes independently. We could also share different method implementations with each other easily, 
which became the backbone of how we worked collaboratively on VanJeckylson.