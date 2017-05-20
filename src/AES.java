/**
 * Main AES Class
 * Contains all common AES encrypt/decrypt data and methods
 */
public abstract class AES {
    // Holds the expanded Key
    protected int[][] expandedKey;
    // Holds the initialization vector.
    protected int[][] initializationVector = new int[4][4];

    /**
     * Expands given key to create individual round keys
     * @param key
     */
    protected int[][] keyExpansion(String key) {
        // Set number of keys we need - 10 keys x 4 bytes + initial
        int keySize = 44;
        // Init rcon pointer
        int rconIndex = 1;
        // Init key variable
        int[][] expandedKey = new int[4][keySize];
        // First parse key into 4x4 matrix
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                expandedKey[j][i] = Integer.parseInt(key.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2)), 16);
            }
        }

        // Set start point - given we have already filled the first key
        int current = 4;
        // Set some temp variables
        int[] a = new int[4];
        int b;
        while (current < keySize) {
            if (current % 4 == 0) {
                // We need to go through the g function
                // First copy last word
                for (b = 0; b < 4; b++) {
                    a[b] = expandedKey[b][current - 1];
                }
                // Go through g
                a = gFunction(a, rconIndex++);
                // XOR with [i-4] word
                for (b = 0; b < 4; b++) {
                    expandedKey[b][current] = a[b] ^ expandedKey[b][current - 4];
                }
            } else {
                // Simply XOR with [i-4]
                for (b = 0; b < 4; b++) {
                    expandedKey[b][current] = expandedKey[b][current - 1] ^ expandedKey[b][current - 4];
                }
            }
            current++;
        }

        return expandedKey;
    }

    /**
     * Helper function used in key expansion
     *
     * For each 4th word in the expanded key
     * This function rotates the word
     * Subs with Sbox
     * And XORS each byte with an rcon constant
     */
    private int[] gFunction(int[] a, int index) {
        int[] tmp = new int[4];

        // Rotate similar to shift rows
        tmp[0] = a[1];
        tmp[1] = a[2];
        tmp[2] = a[3];
        tmp[3] = a[0];

        // Sub with sBox
        int val;
        for (int i = 0; i < 4; i++) {
            val = tmp[i];
            tmp[i] = Constants.sbox[val / 16][val % 16];
        }

        // Finally XOR with rcon
        tmp[0] ^= Constants.rcon[index];

        return tmp;
    }

    /**
     * Adds round key to state via XOR
     */
    protected int[][] addRoundKey(int[][] state, int round) {
        // First need to get the round key from key matrix
        int[][] roundKey = new int[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                roundKey[i][j] = this.expandedKey[i][(4 * round) + j];
            }
        }

        // Now XOR roundKey with state
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] ^= roundKey[i][j];
            }
        }

        return state;
    }

    /**
     * Mix columns via galois multiplication
     *
     * Each byte is transformed via galois multiplication
     * To do this we will use multiplication lookup tables
     * The lookup index is based off the bytes position in state
     * And its comparison to a galois constant array
     */
    protected int[][] mixColumns(int[][] state) {
        // Init temp array
        int[][] tmp = new int[4][4];
        // Iterate twice to parse state
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int val = 0;

                // The lookup result of each rows bytes is XORed, so iterate the row
                for (int k = 0; k < 4; k++) {
                    // Get galois value
                    int g = Constants.galois[i][k];
                    // Get state value
                    int s = state[k][j];
                    // Use galois multiplication lookups
                    if (g == 1) {
                        val = val ^ s;
                    } else if (g == 2) {
                        val = val ^ Constants.mc2[s / 16][s % 16];
                    } else if (g == 3) {
                        val = val ^ Constants.mc3[s / 16][s % 16];
                    } else {
                        val = val ^ 0;
                    }
                }
                // Insert result into temp state
                tmp[i][j] = val;
            }
        }

        return tmp;
    }

    /**
     * Inverse mix columns
     *
     * This is the inverse of mixColumns
     * See above for operations
     */
    protected int[][] invMixColumns(int[][] state) {
        int[][] tmp = new int[4][4];
        // Loop the 2D array
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                // Setup temp calc value for sumations
                int val = 0;
                // The lookup result of each rows bytes is XORed, so iterate the row
                for (int k = 0; k < 4; k++) {
                    // Get galois value
                    int g = Constants.invgalois[i][k];
                    // Get state value
                    int s = state[k][j];
                    // Use galois multiplication lookups
                    if (g == 1) {
                        val = val ^ s;
                    } else if (g == 9) {
                        val = val ^ Constants.mc9[s / 16][s % 16];
                    } else if (g == 11) {
                        val = val ^ Constants.mc11[s / 16][s % 16];
                    } else if (g == 13) {
                        val = val ^ Constants.mc13[s / 16][s % 16];
                    } else if (g == 14) {
                        val = val ^ Constants.mc14[s / 16][s % 16];
                    } else {
                        val = val ^ 0;
                    }
                }
                // Insert result into temp state
                tmp[i][j] = val;
            }
        }

        return tmp;
    }

    /**
     * Shifts rows in state
     *
     * Row 0 is left untouched
     * Row 1 shifts 1 left
     * Row 2 shifts 2 left
     * Row 3 shifts 3 left
     */
    protected int[][] shiftRows(int[][] state) {
        // Init temp matrix
        int[][] tmp = new int[4][4];

        // Row 0 is untouched
        tmp[0][0] = state[0][0];
        tmp[0][1] = state[0][1];
        tmp[0][2] = state[0][2];
        tmp[0][3] = state[0][3];

        // Shift row 1 left 1 position
        tmp[1][0] = state[1][1];
        tmp[1][1] = state[1][2];
        tmp[1][2] = state[1][3];
        tmp[1][3] = state[1][0];

        // Shift row 2 left 2 positions
        tmp[2][0] = state[2][2];
        tmp[2][1] = state[2][3];
        tmp[2][2] = state[2][0];
        tmp[2][3] = state[2][1];

        // Shift row 3 left 3 positions
        tmp[3][0] = state[3][3];
        tmp[3][1] = state[3][0];
        tmp[3][2] = state[3][1];
        tmp[3][3] = state[3][2];

        return tmp;
    }

    /**
     * Shifts rows in state
     *
     * Row 0 is left untouched
     * Row 1 shifts 1 untouched
     * Row 2 shifts 2 untouched
     * Row 3 shifts 3 untouched
     */
    protected int[][] invShiftRows(int[][] state) {
        // Init temp matrix
        int[][] tmp = new int[4][4];

        // Row 0 is untouched
        tmp[0][0] = state[0][0];
        tmp[0][1] = state[0][1];
        tmp[0][2] = state[0][2];
        tmp[0][3] = state[0][3];

        // Shift row 1 right 1 position
        tmp[1][0] = state[1][3];
        tmp[1][1] = state[1][0];
        tmp[1][2] = state[1][1];
        tmp[1][3] = state[1][2];

        // Shift row 2 right 2 positions
        tmp[2][0] = state[2][2];
        tmp[2][1] = state[2][3];
        tmp[2][2] = state[2][0];
        tmp[2][3] = state[2][1];

        // Shift row 3 right 3 positions
        tmp[3][0] = state[3][1];
        tmp[3][1] = state[3][2];
        tmp[3][2] = state[3][3];
        tmp[3][3] = state[3][0];

        return tmp;
    }

    /**
     * Substitute key bytes with bytes from the S-Box
     * @param state
     */
    protected int[][] subBytes(int[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int hexValue = state[i][j];
                state[i][j] = Constants.sbox[hexValue / 16][hexValue % 16];
            }
        }

        return state;
    }

    /**
     * Substitute key bytes with bytes from the inverse S-Box
     * @param state
     */
    protected int[][] invSubBytes(int[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int hexValue = state[i][j];
                state[i][j] = Constants.rsbox[hexValue / 16][hexValue % 16];
            }
        }

        return state;
    }

    /**
     * Helper function to perform deep copies on 2D array
     */
    protected int[][] deepCopyState(int[][] state) {
        int[][] tmp = new int[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tmp[i][j] = state[i][j];
            }
        }

        return tmp;
    }

    /**
     * Parse IV into a useful block array
     */
    public void parseIV(String iv) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                this.initializationVector[j][i] = Integer.parseInt(iv.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2)), 16);
            }
        }
    }

    /**
     * Converts integer array state to a string
     */
    protected String toString(int[][] state) {
        String output = "";

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                String k = Integer.toHexString(state[j][i]).toUpperCase();
                if (k.length() == 1) {
                    output += '0' + k;
                } else {
                    output += k;
                }
                // Append a space
                output += ' ';
            }
        }

        return output;
    }

    /**
     * Abstract class for encryption
     * To be implemented on an Encryption mode basis
     */
    public abstract String encrypt(String input);

    /**
     * Abstract class for decryption
     * To be implemented on a Decryption mode basis
     */
    public abstract String decrypt(String input);
}
