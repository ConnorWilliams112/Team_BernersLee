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
system, enemy tracking, and variable tactics for the different types of engagements. For dynamic 
targeting, we estimate future enemy position using linear prediction based on bullet travel time, 
and enemy direction / speed. We vary the firepower based on these factors and enemy distance so 
the bot fires more aggressively at closer targets and weaker shots for lower confidence targets.
The movement system is the most complex part of the code and is primarily how we implement different 
tactics for 1v1 vs melee. We created a hybrid movement system using "antigravity" (which uses repulsive 
forces that "push" the bot away from walls and enemies) and a more fixed, aggressive movement system 
that strafes the enemy and incorporates some random direction switches. When there are more than 2 
enemy bots in the arena, VanJeckylson biases more towards conservative antigravity-style movement in 
order to preserve itself longer. When the field drops down to 2 or less enemy, VanJeckylson biases 
more towards the aggressive, deterministic movement. Neither movement system is entirely disregarded 
at any point, however, allowing our bot to keep the best of both types but adjust to its circumstances.

### Object Oriented Programming

[Describe how you worked on understaning the RoboCode library. Did you use the 
JavaDocs? Did you just go by what was on the Readme. Specifically mention if
the object oriented nature of Java was beneficial or harmful for your ease 
of implementation.]



## Generative AI and LLM Consultation

[Detail what AI tools you used]

## Challenges

[Describe the challenges of working within the a strict object oriented paradigm.
Do you feel like it overly constrained your algorithmic design or did it help
your conceptualization of the task. Were there any awkward parts which would have
been easier to implement?]

## Critical Thinking

> **Object Oriented Programming and modularization**
>
> When implementing your Robocode robot using an object-oriented paradigm, consider how the structure of 
> your code shaped the way you and your partner divided and coordinated your work. In your response, 
> address the following:
>
> 1. How did encapsulating behaviors into classes and methods (e.g., movement logic, targeting, or event 
>    handling) affect how you and your partner split responsibilities? Would the same division of 
>    work have been natural in a purely imperative approach?
> 2. Robocode provides an upstream class hierarchy (e.g., Robot or AdvancedRobot) that your robot 
>    extends. How did inheriting from these base classes both enable and constrain your design 
>    decisions? Were there moments where the upstream API guided or changed your intended approach?
> 3. Reflect on the concept of a contract in OOP — the expectation that a method or class behaves in a 
>    predictable way. How did defining (or relying on) these contracts between your own classes affect how 
>    you and your partner could work independently without constantly syncing?

1. The classes and methods made it simple to divide the robot into behaviors. Both of us could work on any of the behaviors at a time, for example, with Connor developing movement logic while Mike worked on targeting. Keeping the rest of the behaviors the same made iterating simpler in that we both would keep the same "starting point" while making our own changes to see what worked. Making a change to `computeVJMovement()` would not affect `pickFirePower()`. 

It would have been more difficult to achieve this in a purely imperative approach because the procedural steps for the robot are not clear when designing its behavior. In this case, without explicit methods and attributes, making a change to movement patterns would require careful design to avoid unintended consequences to targeting.

2. Inheriting from the `Bot` class gave us a clear starting point for our code. It allowed us to abstract away the low-level Robocode-isms and focus on the hooks to begin our implementation. `run()`, `onScannedBot()`, and `onHitWall()`, for example, make it clear to the developer how our bot interacts in the battle. This gave a simple, event-driven skeleton for developing our bot. 

A couple challenges did come from using the API. When designing the antigravity movement pattern, we could have imported from `robocode.util.Utils` and the implementation would have been far easier. However, the API we were working with did not allow this import. Another complexity of using the API was that the radar and gun movement are tied together. We had to design scanning and aiming around turning the gun. It would be beneficial to be able to scan without moving the gun and only moving the gun on request.

3. The "contract" concept of OOP made collaborative work a lot simpler than dealing with pure imperative programming. Each method had a purpose that should not interfere with other methods. As mentioned in our algorithm development, keeping method responsibilities separate ensured we could each develop on our own and test changes independently. We could also share different method implementations with each other easily, which became the backbone of how we worked collaboratively on VanJeckylson.