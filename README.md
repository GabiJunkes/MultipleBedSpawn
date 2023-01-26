# MultipleBedSpawn
A simple Minecraft plugin to allow players to choose which bed to respawn into.

# How it works

When a player dies, if they have at least one bed, a respawn menu appears so they can choose where to respawn. With which bed displaying the location and cooldown timer if enabled.

A player can set a bed by right clicking it.

And rename it with the command ```/multibed 'the name they want'``` while looking at the bed.

![image](https://user-images.githubusercontent.com/69057368/210019366-3a981d52-79a2-4bfd-9217-0aac37918243.png)

# Configuration

The plugin has 6 options

<strong>lang</strong> - Chooses the language of the plugin, at the moment we have 2, English (enUS) and Portuguese (ptBR), but you can view all the supported languages at the folder ```src/main/resources/language``` inside this repo.

<strong>max-beds</strong> - The maximum number of beds a player can have.

<strong>disable-sleeping</strong> - If players can sleep or not (if set to true, player will not be able to lie in bed).

<strong>bed-cooldown</strong> - The amount of time, in seconds, players have to wait before respawning in the same bed.

You can select which worlds the plugin will run. Choosing between allowlist or denylist (you can use both, but it does not make sense).
Leave empty to work in every world.

<strong>denylist</strong> - List of world where the plugin will not work.

<strong>allowlist</strong> - List of world where the plugin will not work.


# Help needed

<strong>Translation:</strong> To help with translating this plugin you will have to:
- Make a fork of this repo
- Copy the ```enUS.yml``` file inside ```src/main/resources/language``` folder
- Rename it to the language you will be translating
- Then replace the strings inside "" to the language you want
- Make a pull request

<strong>Reminder:</strong> This is a side project of mine, but feel free to suggest features and if I like it, I might implement it (or maybe you can!). Also you can report a bug in the issues section.

# Why?

I created this plugin because I did not find anything similar, so I learned how to create a Minecraft plugin and made it. I got inspiration in the respawn system of the game <strong>Rust</strong> from <strong>Facepunch</strong>. I made it open source, so people can help translate it, report bugs and improve it. I hope it helps you and that you have fun with it. :)
