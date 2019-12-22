# ZHAW HS19 – MOBA1 Project



### Team members:

- Tram Anh Duong (duongtra, IT16ta_ZH)
- Vanessa Haas (haasvan1, IT16ta_ZH)





## Guess Who? – Game idea / concept

*“**Guess Who?**”*: a two player game, where players use yes/no questions to isolate a hidden character. The first player to guess the other players hidden character, wins.

Each player has a game board of 16 faces with names and picks one of them as the hidden character. This character represents the figure your opponent has to guess and you have to answer questions about.

One of the players (randomly chosen) goes first, beginning by asking the other player a characteristic found on one of the 16 visible figures. If the other player says “yes”, the asking player flips over all of the faces without the asked characteristic. Through the process of elimination, players will eventually be able to “guess” the name of the opponent's character.

Each player gets one yes/no question per turn and may only guess (to win the game) once per round. If a player successfully guesses the opponent’s hidden character, he/she wins.

[Source, 02.10.2019: [Guess who? rules](https://howdoyouplayit.com/guess-game-rules-play-guess-who/)]



## Gameplay details

### *Basic design*

Check our [prototype](https://app.moqups.com/iZEJYIM9Q0/view/page/aa9df7b72) (designed on moqups web application).

Click on the *Comments* button to read the guide/workflow of the prototype.

### How many players

2 online players, or 1 against the CPU (for automated test/demo).

### How many devices

2 android phones / 1 phone and 1 emulator.



## Technologies

For this project, we need the following technologies.

| **Android phone**           | for the project target device, the final app will run on an Android phone, Google Pixel 3. |
| --------------------------- | ------------------------------------------------------------ |
| **Android Studio Emulator** | since only one of us has an Android phone, while developing the app for this project, we will also use the emulator integrated in the Android Studio editor. |



## Development Technologies / Pipeline Tools

To develop our application, we use the following development technologies.

| **Github**                             | to collaborate in the development of our application, we use Github as the remote repository workplace, with its project management tools (pull request, wiki, …). |
| -------------------------------------- | ------------------------------------------------------------ |
| **Zenhub**                             | to track the user stories to be implemented and manage the milestones. |
| **CircleCI**                           | Continuous Integration and Delivery tool to check the features readiness and to manage the automated deployment. |
| **Firebase**                           | a Google Server Platform on which we delegate the backend (auth/) real-time sync management. |
| **Android Studio**                     | possible editor to implement our app & run an integrated emulator. |
| **Moqups**                             | we use Moqups to design our application.                     |
| **Codename One**                       | possible framework for frontend development. [[Codename One](https://github.com/codenameone/CodenameOne)] |
| **Kotlin** $\textcolor{white}{…………….}$ | we plan to implement our application in Kotlin, because we would like to learn a new programming language, and it looks cool in our CV :) |