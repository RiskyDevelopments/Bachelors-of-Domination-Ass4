package sepr.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import sepr.game.saveandload.SaveLoadManager;
import sepr.game.utils.PunishmentCardType;

import java.util.List;

/**
 * class that produces reusable dialog windows for displaying information to the player and receiving input from them
 */
public class DialogFactory {

    private static Skin skin; // skin shared by all dialog windows for a uniform aesthetic

    public DialogFactory() {
        skin = new Skin(Gdx.files.internal("dialogBox/skin/uiskin.json"));
    }

    /**
     * creates a dialog in the given stage with an ok button and the given title and message
     * the dialog displays until the okay button is pressed then it quickly fades away
     *
     * @param title String to be used at the top of the dialog box
     * @param message String to be used as the content of the dialog
     * @param stage to draw the box onto
     */
    public static void basicDialogBox(String title, String message, Stage stage) {
        Dialog dialog = new Dialog(title, DialogFactory.skin);
        dialog.text(message);
        basicStyle(dialog, 60);
        dialog.button("Ok", "0");
        dialog.show(stage);
    }

    /**
     * Used to uniformly style buttons
     * @param dialog Dialog box to style
     * @param width Width of each button
     */
    private static void basicStyle(Dialog dialog, int width){
        dialog.getButtonTable().defaults().width(width);
        dialog.getButtonTable().row().padBottom(10);
    }

    /**
     * creates a dialog box displaying which players turn it is next and how many troops they have to allocate
     * the dialog displays until the okay button is pressed then it quickly fades away
     *
     * @param nextPlayer String to be used to display the name of the next player
     * @param troopsToAllocate Integer to be used to display number of troops the player has to allocate
     * @param stage to draw the box onto
     */
    public static void nextTurnDialogBox(String nextPlayer, Integer troopsToAllocate, Stage stage) {
        basicDialogBox("Next Turn", "Next Player: " + nextPlayer + "\nTroops to Allocate: " + troopsToAllocate, stage);
    }

    /**
     * creates a dialog where the player can confirm if they want to exit the program
     *
     * @param stage The stage to draw the box onto
     */
    public static void exitProgramDialogBox(Stage stage) {
        Dialog dialog = new Dialog("Quit", DialogFactory.skin) {
            protected void result(Object object) {
                if (object.toString().equals("1")){ // yes pressed : quit the game
                    Gdx.app.exit(); // close the program
                }
            }
        };
        basicStyle(dialog, 60);
        dialog.text("Are you sure you want to exit the game?");
        dialog.button("Yes", "1");
        dialog.button("No", "0");
        dialog.show(stage);
    }

    /**
     * creates a dialog where the player can confirm if they want to exit the program
     *
     * @param stage The stage to draw the box onto
     */
    public static void exitMinigame(Stage stage, final GameScreen gameScreen, final Main main) {
        Dialog dialog = new Dialog("Quit", DialogFactory.skin) {
            protected void result(Object object) {
                if (object.toString().equals("1")){ // yes pressed : quit the game
                    main.setScreen(gameScreen);  // close the program
                    gameScreen.resetCameraPosition();

                }
            }
        };
        dialog.text("Are you sure you want to exit the mini game?");
        dialog.button("Yes", "1");
        dialog.button("No", "0");
        dialog.show(stage);
    }


    /**
     * creates a dialog where the player can confirm if they want to leave the current game
     * if yes pressed then the screen is changed to the main menu
     * if no pressed then the screen stays the same and the dialog closes
     *
     * @param gameScreen for changing the screen
     * @param stage the stage to draw the box onto
     */
    public static void leaveGameDialogBox(final GameScreen gameScreen, Stage stage) {
        Dialog dialog = new Dialog("Quit", DialogFactory.skin) {
            protected void result(Object object) {
                if (object.toString().equals("1")){ // yes pressed therefore quit the game
                    gameScreen.openMenu(); // change screen to menu screen
                }
            }
        };
        basicStyle(dialog, 60);
        dialog.text("Are you sure you want to exit the game?");
        dialog.button("Yes", "1");
        dialog.button("No", "0");
        dialog.show(stage);
    }

    /**
     * Dialog box for the pause menu
     *
     * @param gameScreen for changing the screen
     * @param stage the stage to draw the box onto
     */
    public static void pauseGameDialogBox(final GameScreen gameScreen, Stage stage) {
        Dialog dialog = new Dialog("Paused", DialogFactory.skin) {
            protected void result(Object object) {
                if (object.equals("save")) {
                    gameScreen.getMain().saveGame();
                } else if (object.equals("saveExit")) {
                    gameScreen.getMain().saveGame();
                    gameScreen.openMenu(); // change screen to menu screen
                } else if (object.equals("exit")) {
                    gameScreen.openMenu(); // change screen to menu screen
                }
            }
        };
        dialog.getButtonTable().defaults().width(180);
        dialog.getButtonTable().row().padBottom(10);
        dialog.button("Resume", "resume");
        dialog.getButtonTable().row().padBottom(10);
        dialog.button("Save Game", "save");
        dialog.getButtonTable().row().padBottom(10);
        dialog.button("Save and Exit", "saveExit");
        dialog.getButtonTable().row().padBottom(10);
        dialog.button("Exit", "exit");
        dialog.show(stage);
    }

    /**
     * creates a dialog that says which player took control of a sector from which other player
     *
     * @param prevOwner name of the player who used to own the sector
     * @param newOwner name of the player who now controls the sector
     * @param sectorName name of the sector being taken
     * @param stage to draw the box onto
     */
    public static void sectorOwnerChangeDialog(String prevOwner, String newOwner, String sectorName, Stage stage) {
        basicDialogBox("Sector Owner Change", newOwner + " gained " + sectorName + " from " + prevOwner, stage);
    }

    /**
     * creates a dialog box with a slider and okay box allowing a player who has conquered a sector to select how many troops to move onto it
     *
     * @param bonusTroops amount of troops the player is awarded for conquering the tile
     * @param maxTroops the amount of troops on the attacking tile
     * @param troopsMoved 3 index final array for setting value of slider to representing how many troops to move: [0] amount of units to move; [1] id of source sector; [2] id of target sector
     * @param prevOwner name of the player who used to own the sector
     * @param newOwner name of the player who now controls the sector
     * @param sectorName name of the sector being taken
     * @param stage The stage to draw the box onto
     */
    public static void attackSuccessDialogBox(Integer bonusTroops, Integer maxTroops, final int[] troopsMoved, String prevOwner, String newOwner, String sectorName, Stage stage) {
        final Slider slider = new Slider(1, (maxTroops - 1), 1, false, DialogFactory.skin); // slider max value is (maxTroops - 1) as must leave at least one troop on attacking sector
        slider.setValue(1); // must move at least one troop so set initial value to 1
        final Label sliderValue = new Label("1", DialogFactory.skin); // label to display the slider value

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sliderValue.setText(new StringBuilder((int)slider.getValue() + "")); // update slider value label when slider moved
            }
        });

        Dialog dialog = new Dialog("Success!", DialogFactory.skin) {
            protected void result(Object object) {
                // set number of troops to move to the value of the slider when the dialog is closed
                troopsMoved[0] = (int)slider.getValue();
            }
        };

        dialog.text(newOwner + " gained " + sectorName + " from " + prevOwner + "\nYou have earned " + bonusTroops + " bonus troops!\nHow many troops would you like to move to the new sector?");
        dialog.getContentTable().row();

        dialog.getContentTable().add(slider).padLeft(20).padRight(20).align(Align.left).expandX();
        dialog.getContentTable().add(sliderValue).padLeft(20).padRight(20).align(Align.right);

        dialog.getContentTable().row();

        basicStyle(dialog, 60);
        dialog.button("Ok", "0");
        dialog.show(stage);
    }

    /**
     * creates a dialog modal allowing the user to select how many units they want to allocate to a sector
     *
     * @param maxAllocation maximum amount of troops that can be assigned
     * @param allocation 2 index array storing : [0] number of troops to allocate ; [1] id of sector to allocate to
     * @param sectorName name of sector being allocated to
     * @param stage to draw the box onto
     */
    public static void allocateUnitsDialog(Integer maxAllocation, final int[] allocation, String sectorName, Stage stage) {
        final Slider slider = new Slider(1, maxAllocation, 1, false, DialogFactory.skin);
        final Label sliderValue = new Label("1", DialogFactory.skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sliderValue.setText(new StringBuilder((int)slider.getValue() + ""));
            }
        });

        Dialog dialog = new Dialog("Select amount of troops to allocate", DialogFactory.skin) {
            protected void result(Object object) {
                if (object.equals("0")) { // Cancel button pressed
                    allocation[0] = -1;
                    allocation[1] = -1; // set allocating sector id to -1 to indicate the allocation has been cancelled
                } else if (object.equals("1")) { // Ok button pressed
                    allocation[0] = (int)slider.getValue(); // set the number of troops to allocate to the value of the slider
                }
            }
        };
        dialog.text("You can allocate up to " + maxAllocation + " troops to " + sectorName);
        dialog.getContentTable().row();

        dialog.getContentTable().add(slider).padLeft(20).padRight(20).align(Align.left).expandX();
        dialog.getContentTable().add(sliderValue).padLeft(20).padRight(20).align(Align.right);

        dialog.getContentTable().row();

        basicStyle(dialog, 90);
        dialog.button("Cancel", "0");
        dialog.button("Ok", "1");
        dialog.show(stage);
    }

    /**
     * creates a dialog box for the player to select how many troops they want to attack with
     * if player cancels the attackers[0] = 0 to signify the attack has been cancelled
     *
     * @param maxAttackers max number of attackers the player chooses to attack with
     * @param defenders how many units are defending
     * @param attackers 1 index array for setting number of troops the player has chosen to attack with: [0] number of troops player has set to attack with
     * @param stage to display the dialog on
     * @return the number of troops chosen to attack with or 0 if the attack is canceled
     */
    public static void attackDialog(int maxAttackers, int defenders, final int[] attackers, Stage stage) {
        final Slider slider = new Slider(0, maxAttackers, 1, false, DialogFactory.skin);
        slider.setValue(maxAttackers);
        final Label sliderValue = new Label(maxAttackers + "", DialogFactory.skin); // label showing the value of the slider
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sliderValue.setText(new StringBuilder((int)slider.getValue() + "")); // update slider value label when the slider is moved
            }
        });

        Dialog dialog = new Dialog("Select number of troops to attack with", DialogFactory.skin) {
            protected void result(Object object) {
                if (object.equals("0")) { // cancel pressed
                    attackers[0] = 0; // set number of attacker to 0, i.e. no attack
                } else if (object.equals("1")){ // ok button pressed
                    attackers[0] = (int)slider.getValue(); // set number of attackers to the value of the slider
                }
            }
        };

        // add labels saying the max number of attackers and how many defenders there are
        dialog.text(new Label("Max attackers: " + maxAttackers, DialogFactory.skin)).padLeft(20).padRight(20).align(Align.left);
        dialog.text(new Label("Defenders: " + defenders, DialogFactory.skin)).padLeft(20).padRight(20).align(Align.right);

        dialog.getContentTable().row();

        // add slider and label showing number of units selected
        dialog.getContentTable().add(slider).padLeft(20).padRight(20).align(Align.left).expandX();
        dialog.getContentTable().add(sliderValue).padLeft(20).padRight(20).align(Align.right);

        dialog.getContentTable().row();

        basicStyle(dialog, 90);
        // add buttons for accepting or canceling the selection
        dialog.button("Cancel", "0").padLeft(20).padRight(40).align(Align.center);
        dialog.button("Ok", "1").padLeft(40).padRight(20).align(Align.center);

        dialog.show(stage);
    }



    /**
     * creates a dialog box for the player to select how many troops they want to move with
     * if player cancels the attackers[0] = 0 to signify the attack has been cancelled
     *
     * @param maxAttackers max number of attackers the player chooses to move
     * @param attackers 1 index array for setting number of troops the player has chosen to move with: [0] number of troops player has set to move with
     * @param stage to display the dialog on
     * @return the number of troops chosen to attack with or 0 if the attack is canceled
     */
    public static void moveDialog(int maxAttackers, final int[] attackers, Stage stage) {
        maxAttackers --; // leave at least one troop on the tile
        final Slider slider = new Slider(0, maxAttackers, 1, false, DialogFactory.skin);
        slider.setValue(maxAttackers);
        final Label sliderValue = new Label(maxAttackers + "", DialogFactory.skin); // label showing the value of the slider
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sliderValue.setText(new StringBuilder((int)slider.getValue() + "")); // update slider value label when the slider is moved
            }
        });

        Dialog dialog = new Dialog("Select number of troops to move", DialogFactory.skin) {
            protected void result(Object object) {
                if (object.equals("0")) { // cancel pressed
                    attackers[0] = 0; // set number of attacker to 0, i.e. no attack
                } else if (object.equals("1")){ // ok button pressed
                    attackers[0] = (int)slider.getValue(); // set number of attackers to the value of the slider
                }
            }
        };

        // add labels saying the max number of attackers and how many defenders there are
        dialog.text(new Label("Max number to move: " + maxAttackers, DialogFactory.skin)).padLeft(20).padRight(20).align(Align.left);
        dialog.getContentTable().row();

        // add slider and label showing number of units selected
        dialog.getContentTable().add(slider).padLeft(20).padRight(20).align(Align.left).expandX();
        dialog.getContentTable().add(sliderValue).padLeft(20).padRight(20).align(Align.right);

        dialog.getContentTable().row();

        basicStyle(dialog, 90);
        // add buttons for accepting or canceling the selection
        dialog.button("Cancel", "0").padLeft(20).padRight(40).align(Align.center);
        dialog.button("Ok", "1").padLeft(40).padRight(20).align(Align.center);

        dialog.show(stage);
    }



    /**
     * dialog that displays a list of players that have been eliminated
     *
     * @param playerNames array of eliminated player names
     * @param stage to draw the box to
     */
    public static void playersOutDialog(String[] playerNames, Stage stage) {
        Dialog dialog = new Dialog("Elimination!", DialogFactory.skin);
        String message = "The following player has been eliminated:";
        if (playerNames.length > 1) {
            message = "The following players have been eliminated:";
        }
        for (String s : playerNames) { // for each eliminated player start a new line and list their name
            message += "\n    " + s;
        }
        dialog.text(message);
        basicStyle(dialog, 60);
        dialog.button("Ok", "0");
        dialog.show(stage);
    }

    /**
     * dialog displayed when a player has won the game showing the name of the player that has won and what college they belong to
     *
     * @param playerName name of player who has won
     * @param collegeName name of the winning player's college
     * @param main for changing back to the menu screen
     * @param stage to draw the dialog to
     */
    public static void gameOverDialog(String playerName, String collegeName, final Main main, Stage stage) {
        Dialog dialog = new Dialog("Game Over!", DialogFactory.skin) {
            protected void result(Object object) {
                main.setMenuScreen(); // change to menu screen when ok button is pressed
            }
        };
        dialog.text("Game Over!\n" + playerName + " of College " + collegeName + " has conquered the University of York!");
        basicStyle(dialog, 60);
        dialog.button("Ok", "0");
        dialog.show(stage);
    }




    /**
     * creates a dialog box displaying informing the player they have taken over the PVC tile
     *
     * @param stage to draw the box onto
     */
    public static void TakenOverPVCDialogue(Stage stage) {
        basicDialogBox("Pro Vice Chancellor tile captured","Well done you have found and captured the Pro Vice Chancellor tile. You now get extra 1 bonus troop per turn from this sector",stage);
    }

    /**
     * creates a dialog box asking if the player wants to exit the mini game
     *
     * @param stage to draw the box onto
     * @param miniGameScreen the mini game screen where the dialog will be shown
     */

    public static void leaveMiniGameDialog(final MiniGameScreen miniGameScreen, Stage stage) {
        Dialog dialog = new Dialog("Continue?", DialogFactory.skin) {
            protected void result(Object object) {
                if (object.toString().equals("0")){ // yes pressed : quit the minigame
                    miniGameScreen.endGame(true);

                }
            }
        };
        dialog.text("Would you like to keep playing or quit now? (One incorrect answer loses all bonuses!)");
        dialog.button("Keep playing", "1");
        dialog.button("Quit", "0");
        dialog.show(stage);
    }


    /**
     * creates a dialog box asking if the player wants to exit the mini game
     *
     * @param main  for changing back to the map
     * @param stage to draw the box onto
     * @param gameScreen the map screen
     * @param rewards list of cards the player has won
     */
    public static void miniGameOverDialog(final Main main, Stage stage, final GameScreen gameScreen, List<PunishmentCardType> rewards) {
        Dialog dialog = new Dialog("Game Completed", DialogFactory.skin) {
            protected void result(Object object) {
                main.setScreen(gameScreen);  // change to menu screen when ok button is pressed
                gameScreen.resetCameraPosition();

            }
        };
        String rewardText = "";

        switch (rewards.size()) {
            case 0:
                rewardText = "The Minigame is over. You failed to earn any rewards";
                break;
            case 1 :
                rewardText = "The Minigame is over and you won The " + rewards.get(0).toString() + "!";
                break;
            case 2:
                rewardText = "The Minigame is over and you won The " + rewards.get(0).toString() + " and The " + rewards.get(1).toString() + "!";
                break;
            case 3:
                rewardText = "The Minigame is over and you won The " + rewards.get(0).toString() + ", The " + rewards.get(1).toString() + " and The " + rewards.get(2).toString() + "!";
                break;
        }

        dialog.text(rewardText);
        basicStyle(dialog, 60);
        dialog.button("Ok", "0");
        dialog.show(stage);
    }

}
