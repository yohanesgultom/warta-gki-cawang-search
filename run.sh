#!/bin/bash

# Run the server in a virtual environment

VIRTUALENV=venv

source "$VIRTUALENV/bin/activate"
python server.py
