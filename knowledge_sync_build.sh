#!/bin/bash

# AIGEN: this was made by AI

# Knowledge Sync Build Script
# This script runs the build process step by step with confirmations

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Array to track completed steps
COMPLETED_STEPS=()

# Parse command line arguments
ACCEPT=false
if [[ "$1" == "--accept" || "$1" == "-a" ]]; then
    ACCEPT=true
fi

# Function to print colored output
print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to ask for confirmation
confirm_step() {
    local message="$1"
    if [[ "$ACCEPT" == "true" ]]; then
        print_info "Running (accept mode): $message"
        return 0
    fi
    
    print_step "$message"
    while true; do
        read -p "Do you want to proceed? (y/n/q): " yn
        case $yn in
            [Yy]* ) return 0;;
            [Nn]* ) print_info "Skipping step..."; return 1;;
            [Qq]* ) print_info "Quitting build process..."; exit 0;;
            * ) echo "Please answer yes (y), no (n), or quit (q).";;
        esac
    done
}

# Function to run a command and report results
run_command() {
    local description="$1"
    local command="$2"
    local directory="$3"
    local step_summary="$4"
    
    if [[ -n "$directory" ]]; then
        print_info "Changing to directory: $directory"
        cd "$directory"
    fi
    
    print_info "Running: $command"
    
    if eval "$command"; then
        print_success "$description completed successfully"
        # Add to completed steps array
        COMPLETED_STEPS+=("$step_summary")
        return 0
    else
        print_error "$description failed"
        return 1
    fi
}

# Get the script directory (should be the knowledge sync repository root)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

print_info "Starting Knowledge Sync build process..."
print_info "Working directory: $(pwd)"

# Step 1: Publish HLC to Maven Local
if confirm_step "Publish HLC to Maven local (Gradle task)"; then
    run_command "HLC Maven local publishing" "./gradlew publishToMavenLocal" "hlc" "✓ Published HLC to Maven local"
    cd "$SCRIPT_DIR"  # Return to root
fi

# Step 2: Build Flutter APK
if confirm_step "Build Flutter example app APK"; then
    run_command "Flutter APK build" "flutter build apk" "graphplugin/example" "✓ Built Flutter example APK"
    cd "$SCRIPT_DIR"  # Return to root
fi

# Step 3: Regenerate JNI bindings
if confirm_step "Regenerate JNI bindings"; then
    run_command "JNI bindings regeneration" "flutter pub run jnigen --config jnigen.yaml" "graphplugin" "✓ Regenerated JNI bindings"
    cd "$SCRIPT_DIR"  # Return to root
fi

# Step 4: Run HLC tests
if confirm_step "Run HLC tests (Kotlin JUnit tests)"; then
    run_command "HLC tests" "./gradlew test" "hlc" "✓ Ran HLC tests (Kotlin JUnit)"
    cd "$SCRIPT_DIR"  # Return to root
fi

# Step 5: Run Kotlin library tests
if confirm_step "Run Kotlin library tests (GraphPlugin unit tests)"; then
    run_command "Kotlin library tests" "./gradlew test" "graphplugin/android" "✓ Ran Kotlin library tests (GraphPlugin unit tests)"
fi

# Step 6: Run Kotlin lint
if confirm_step "Run Kotlin lint"; then
    run_command "Kotlin lint" "ktlint -F" "." "✓ Ran Kotlin lint"
fi

# Final summary
echo
if [[ ${#COMPLETED_STEPS[@]} -eq 0 ]]; then
    print_info "No steps were completed."
else
    print_success "Completed ${#COMPLETED_STEPS[@]} step(s) successfully!"
    print_info "Summary of completed steps:"
    
    for step in "${COMPLETED_STEPS[@]}"; do
        print_info "$step"
    done
fi

if [[ "$ACCEPT" == "false" ]]; then
    echo
    print_info "To run this script without confirmations, use: $0 --accept"
    print_info "At any prompt, you can type 'q' to quit the build process"
fi 