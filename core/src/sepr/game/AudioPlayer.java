package sepr.game;

import com.badlogic.gdx.audio.Sound;

import java.util.Random;

/*
Added in assessment 4
 - created to enable easy triggering of sound effects
 - created to make it easier to set what music is playing and swapping between music now just requires triggering the method of the new track you want to play
 - moved selecting what sound effect to play to here to reduce code duplication for when selecting sound from the same set of clips
 */

/**
 * class for being able to trigger sound effects and set music to play
 */
public class AudioPlayer {
    private static AudioManager Audio = AudioManager.getInstance(); // Access to the AudioManager
    private static Random random = new Random();

    public AudioPlayer() {
        Audio.loadSounds(); //loads the sounds into memory
    }

    /**
     * plays a random audio file from the file paths specified in the audioFiles array
     * noAudioChances specifies the chance no audio file is played
     *
     * @throws IllegalArgumentException if length of AudioFiles is 0
     * @param audioFiles file paths of audio files that could be played
     * @param noAudioChances chance no audio file is played
     */
    private static void selectAudioToPlay(String[] audioFiles, int noAudioChances) {
        if (audioFiles.length == 0) throw new IllegalArgumentException("must be at least one audio file to select from");
        int fileNum = random.nextInt(audioFiles.length + noAudioChances);
        if (fileNum < audioFiles.length) {
            Audio.get(audioFiles[fileNum], Sound.class).play(AudioManager.GLOBAL_FX_VOLUME);
        }
    }

    /**
     * plays an audio clip for when the game is over
     */
    public static void playGameOverAudio() {
        selectAudioToPlay(new String[]{
                "sound/Victory/Colin_Congratulations.wav",
                "sound/Victory/Colin_Congratulations_your_grandson_would_be_proud_of_you.wav",
                "sound/Victory/Colin_Well_Done.wav",
                "sound/Victory/Colin_You_are_victorious.wav"
        }, 0);
    }

    /**
     * plays an audio clip for when a player has been eliminated
     */
    public static void playPlayerEliminatedAudio() {
        Audio.get("sound/Minigame/Colin_That_was_a_poor_performance.wav", Sound.class).play(AudioManager.GLOBAL_FX_VOLUME);
    }

    /**
     * plays an audio clip for when the player has made a bad move
     */
    public static void playBadMoveAudio() {
        selectAudioToPlay(new String[]{
                "sound/Invalid Move/Colin_Your_actions_are_questionable.wav",
                "sound/Battle Phrases/Colin_Seems_Risky_To_Me.wav",
        }, 2);
    }

    /**
     * plays an audio clip for when the player has made a good move
     */
    public static void playGoodMoveAudio() {
        selectAudioToPlay(new String[]{
                "sound/Battle Phrases/Colin_An_Unlikely_Victory.wav",
                "sound/Battle Phrases/Colin_Far_better_than_I_expected.wav",
                "sound/Battle Phrases/Colin_I_couldnt_have_done_it_better_myself.wav",
                "sound/Battle Phrases/Colin_Multiplying_by_the_identity_matrix_is_more_fasinating_than_your_last_move.wav",
                "sound/Battle Phrases/Colin_Well_Done.wav"
        }, 0);
    }

    /**
     * plays an audio clip for when the player has made an invalid move
     */
    public static void playInvalidMoveAudio() {
        selectAudioToPlay(new String[]{
                "sound/Invalid Move/Colin_Your_request_does_not_pass_easily_through_my_mind.wav",
                "sound/Invalid Move/Colin_You_would_find_more_success_trying_to_invert_a_singular_matrix.wav",
                "sound/Invalid Move/Colin_Your_actions_are_questionable.wav",
                "sound/Allocation/Colin_EmptySet.wav"
        }, 2);
    }

    /**
     * plays an audio clip for when the player has tried to make an invalid troop allocation
     */
    public static void playAllocationErrorAudio() {
        selectAudioToPlay(new String[]{
                "sound/Allocation/Colin_EmptySet.wav",
                "sound/Allocation/Colin_Insuffiecient_Gangmembers.wav",
                "sound/Allocation/Colin_Might_I_interest_you_in_taking_the_union_of_our_forces.wav",
                "sound/Invalid Move/Colin_Your_request_does_not_pass_easily_through_my_mind.wav",
                "sound/Invalid Move/Colin_You_would_find_more_success_trying_to_invert_a_singular_matrix.wav",
                "sound/Invalid Move/Colin_Your_actions_are_questionable.wav"
        }, 3);
    }

    /**
     * plays the button click sound effect
     */
    public static void playButtonClick() {
        Audio.get("sound/Other/Electro button click.mp3", Sound.class).play(AudioManager.GLOBAL_FX_VOLUME); //plays the music
    }

    /**
     * stops the current music, if any is playing, and plays the menu music
     */
    public static void playMenuMusic() {
        Audio.loadMusic("sound/IntroMusic/Tron style music - Original track.mp3"); //load and play main menu music
    }

    /**
     * stops the current music, if any is playing, and plays the game music
     */
    public static void playMainGameMusic() {
        Audio.loadMusic("sound/Gameplay Music/80's Retro Synthwave Intro Music.mp3"); //loads and plays the gamePlay music
    }
}
