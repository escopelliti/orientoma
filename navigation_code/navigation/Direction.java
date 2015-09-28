package it.asp.orientoma.navigation;

/**
 * Created by Filippo on 23/05/2015.
 *
 * Possible outputs of the navigation system. The output system translates these codes to messages
 * to the user.
 */
public enum Direction {
    FORWARD,
    LEFT,
    RIGHT,
    BACKWARD,
    TARGET //This is sent when the use reaches the destination
}
