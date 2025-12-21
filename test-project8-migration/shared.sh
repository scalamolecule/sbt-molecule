#!/bin/bash
# Common functions for interactive migration workflow scripts

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Wait for user to press a key
wait_for_user() {
    echo -e "\n${CYAN}Press any key to continue (or 'q' to quit)${NC}"
    old_tty_settings=$(stty -g)
    stty raw -echo
    key=$(dd bs=1 count=1 2>/dev/null)
    stty "$old_tty_settings"
    if [ "$key" = "q" ] || [ "$key" = "Q" ]; then
        echo -e "\n${YELLOW}Exiting...${NC}"
        exit 0
    fi
}
