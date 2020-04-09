#define EMPTY_TILE 0

#define DRAW      0
#define PLAYER    1
#define OPPONENT  2

/**
 * Returns the number of bits in an ulong.
 * (can be optimized by not using a while loop)
 */
int bit_count(unsigned long value)
{
    int result = 0;

    //while(value != 0ULL)
    for (int i = 0; i < 64; i++) {
        result += (int)(value & 1);
        value = value >> 1;
    }

    return result;
}

/**
 * Returns the nth bit in an ulong.
 */
bool get_nth_bit(unsigned long value, int index)
{
    return (value & (1ULL << index)); //return (int) ((value >> index) & 1ULL);
}

/**
 * Checks if the board does not contain any pieces that somehow overlap.
 */
bool is_valid_board(unsigned long player, unsigned long opponent)
{
    return (player & opponent) == 0ULL;
}

/**
 * Returns the type of tile at the given cell.
 * 0 is empty 1 is the current player, 2 is the opponent.
 */
int get_tile_type(int cell, unsigned long player, unsigned long opponent)
{
    if (get_nth_bit(player, cell))
        return PLAYER;

    if (get_nth_bit(opponent, cell))
        return OPPONENT;

    return EMPTY_TILE;
}

/**
 * Return if a player piece is at the end of this direction.
 */
bool is_players_piece_on_the_end(int cell, int direction_row, int direction_column, unsigned long player, unsigned long opponent)
{
    int current_cell = cell;

    // Can be replaced with (get_nth_bit(opponent, cell) == 1) if optimisation somehow fails
    while (get_tile_type(current_cell, player, opponent) == OPPONENT) {
        int row = current_cell / 8;
        int column = current_cell % 8;
        int current_row = row + direction_row;
        int current_column = column + direction_column;

        if (!(current_row >= 0 && current_row < 8) || !((current_column >= 0 && current_column < 8)))
            return false;

        current_cell = (current_row * 8) + current_column;
    }

    // Can be replaced with (get_nth_bit(player, cell) == 1) if optimisation somehow fails
    return get_tile_type(current_cell, player, opponent) == PLAYER;
}

/**
 * Checks if the given tile is a valid move.
 */
bool is_correct_move(int cell, unsigned long player, unsigned long opponent)
{
    int row = cell / 8;
    int column = cell % 8;

    if (!(row >= 0 && row < 8) || !((column >= 0 && column < 8)))
        return false;

    if (get_tile_type(cell, player, opponent) != EMPTY_TILE)
        return false;

    //                                                NW,  N, NE,  W, E, SW, S, SE
    int row_directions[] = { -1, -1, -1, 0, 0, 1, 1, 1 };
    int column_directions[] = { -1, 0, 1, -1, 1, -1, 0, 1 };
    for (int i = 0; i < 8; i++) {
        int direction_row = row_directions[i];
        int direction_column = column_directions[i];

        int current_row = row + direction_row;
        int current_column = column + direction_column;

        if (!(current_row >= 0 && current_row < 8) || !((current_column >= 0 && current_column < 8)))
            continue;

        int current_cell = (current_row * 8) + current_column;

        if (get_tile_type(current_cell, player, opponent) != OPPONENT)
            continue;

        if (is_players_piece_on_the_end(current_cell, direction_row, direction_column, player, opponent))
            return true;
    }

    return false;
}

/**
 * Checks if a given move is valid for the given player.
 */
bool valid_move_available(unsigned long player, unsigned long opponent)
{
    for (int i = 0; i < 64; i++) {
        if (is_correct_move(i, player, opponent))
            return true;
    }

    return false;
}

/**
 * Switches the pointers of the two player.
 * Essentially makes the opponent the new player
 * and the player the new opponent.
 */
void pass_turn(unsigned long players[])
{
    unsigned long tempState = players[0];
    players[0] = players[1];
    players[1] = tempState;
    /*unsigned long tmp = *opponent;
  *opponent = *player;
  *player = tmp;*/
}

unsigned long get_flip_mask(int cell, int direction_row, int direction_column, unsigned long player, unsigned long opponent)
{
    unsigned long result = 0ULL;
    int current_cell = cell;

    while (get_tile_type(current_cell, player, opponent) == OPPONENT) {
        result = result | (1ULL << current_cell);

        int row = current_cell / 8;
        int column = current_cell % 8;

        int current_row = row + direction_row;
        int current_column = column + direction_column;

        current_cell = (current_row * 8) + current_column;
    }

    return result;
}

/**
 * This method changes a tile on the board to the given player.
 */
void make_move(int cell, unsigned long players[])
{
    int row = cell / 8;
    int column = cell % 8;

    unsigned long flip_mask = 0ULL;

    //                                                NW,  N, NE,  W, E, SW, S, SE
    int row_directions[] = { -1, -1, -1, 0, 0, 1, 1, 1 };
    int column_directions[] = { -1, 0, 1, -1, 1, -1, 0, 1 };
    for (int i = 0; i < 8; i++) {
        int direction_row = row_directions[i];
        int direction_column = column_directions[i];

        int current_row = row + direction_row;
        int current_column = column + direction_column;

        if (!(current_row >= 0 && current_row < 8) || !((current_column >= 0 && current_column < 8)))
            continue;

        int current_cell = (current_row * 8) + current_column;

        if (get_tile_type(current_cell, players[0], players[1]) != OPPONENT)
            continue;

        if (is_players_piece_on_the_end(current_cell, direction_row, direction_column, players[0], players[1]))
            flip_mask = flip_mask | get_flip_mask(current_cell, direction_row, direction_column, players[0], players[1]);
    }

    unsigned long new_player_pieces = players[1] ^ flip_mask;
    unsigned long new_opponent_pieces = (players[0] ^ flip_mask) | (1ULL << cell);

    players[0] = new_player_pieces;
    players[1] = new_opponent_pieces;
}

int get_winner(unsigned long player, unsigned long opponent)
{
    int bit_count_player = bit_count(player);
    int bit_count_opponent = bit_count(opponent);

    if (bit_count_player > bit_count_opponent)
        return PLAYER;

    if (bit_count_player < bit_count_opponent)
        return OPPONENT;

    return DRAW;
}

/**
 * Evaluates a board from begin state to end state
 * and returns the winning player.
 */
int evaluate_board(__private unsigned long players[], __global const unsigned int* random_numbers, __private int global_id)
{
    int result_multiplier = 1;

    int correct_moves[64];
    int no_move_available_counter = 0;
    for (int i = 0; i < 64; i++) {
        if(no_move_available_counter > 1)
            break;

        if (!valid_move_available(players[0], players[1])) {
            pass_turn(players);
            no_move_available_counter++;
        } else {
            no_move_available_counter = 0;
            unsigned int idx = 0;
            for (int j = 0; j < 64; j++) {
                if (is_correct_move(j, players[0], players[1]))
                    correct_moves[idx++] = j;
            }

            __private unsigned int element_number = random_numbers[global_id * 64 + i] % idx;
            make_move(correct_moves[element_number], players);
        }

        result_multiplier *= -1;
    }

    switch (get_winner(players[0], players[1])) {
    case PLAYER:
        return 1 * result_multiplier;
    case OPPONENT:
        return -1 * result_multiplier;
    case DRAW:
        return 0;
    }

    return -1;
}

/*
 * The entry-point for this kernel.
 */
__kernel void mctsKernel(
    __global const unsigned long* player_tiles,
    __global const int* possible_moves,
    __global const unsigned int* random_numbers,
    __global int* results,
    __global unsigned long* game_result)
{
    // Retrieve the global_id of this thread.
    __private int global_id = get_global_id(0);

    // Copy the global tile positions to a local array.
    __private unsigned long players[2];
    players[0] = player_tiles[0];
    players[1] = player_tiles[1];

    // Pass the thread's result of this evaluation into the results buffer.
    results[global_id] = evaluate_board(players, random_numbers, global_id);

    if (global_id == 0) {
        game_result[0] = players[0];
        game_result[1] = players[1];
    }
}
