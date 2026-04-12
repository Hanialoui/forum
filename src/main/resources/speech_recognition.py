"""
Python Speech Recognition Script for Forum Microservice
Called directly from Java via ProcessBuilder
"""

import sys
import json
import speech_recognition as sr
import tempfile
import os

def transcribe_audio(audio_path):
    """
    Transcribe audio file to text
    """
    try:
        recognizer = sr.Recognizer()
        
        with sr.AudioFile(audio_path) as source:
            audio_data = recognizer.record(source)
        
        # Use Google Web Speech API (free)
        text = recognizer.recognize_google(audio_data, language='en-US')
        
        result = {
            "success": True,
            "text": text,
            "error": ""
        }
        print(json.dumps(result))
        
    except sr.UnknownValueError:
        result = {
            "success": False,
            "text": "",
            "error": "Could not understand audio"
        }
        print(json.dumps(result))
        
    except sr.RequestError as e:
        result = {
            "success": False,
            "text": "",
            "error": "Speech recognition service unavailable"
        }
        print(json.dumps(result))
        
    except Exception as e:
        result = {
            "success": False,
            "text": "",
            "error": str(e)
        }
        print(json.dumps(result))

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"success": False, "text": "", "error": "No audio file path provided"}))
        sys.exit(1)
    
    audio_path = sys.argv[1]
    transcribe_audio(audio_path)
