#!/usr/bin/env python3
"""Generates the UI sound effects that SoundPoolSoundEffects plays, as short mono WAV files.

The sounds are synthesized here so the app ships no third-party audio. Rerun it after changing
a sound; the output is deterministic.

usage: tools/generate-sounds.py [output dir, default core/audio/src/main/res/raw]
"""
import math
import os
import struct
import sys
import wave

RATE = 22050


def note(freq, seconds, volume=0.5, attack=0.008, harmonics=((1, 1.0), (2, 0.25), (3, 0.08)), detune=0.0):
    """A plucked-bell style tone: quick attack, exponential decay, a few soft harmonics."""
    count = int(RATE * seconds)
    samples = []
    for i in range(count):
        t = i / RATE
        envelope = min(1.0, t / attack) * math.exp(-5.0 * t / seconds)
        value = 0.0
        for multiple, weight in harmonics:
            value += weight * math.sin(2 * math.pi * freq * multiple * t)
            if detune:
                value += weight * 0.5 * math.sin(2 * math.pi * (freq + detune) * multiple * t)
        samples.append(value * envelope * volume)
    return samples


def sequence(*parts, gap=0.0):
    """Notes one after another, each starting [gap] seconds before the previous one ends (overlap)."""
    out = []
    for part in parts:
        start = max(0, len(out) - int(RATE * gap))
        needed = start + len(part)
        out.extend([0.0] * (needed - len(out)))
        for i, s in enumerate(part):
            out[start + i] += s
    return out


def write(path, samples):
    peak = max(abs(s) for s in samples) or 1.0
    scale = 0.8 / peak
    # A few ms fade-out so the end never clicks
    fade = int(RATE * 0.01)
    for i in range(fade):
        samples[-1 - i] *= i / fade
    with wave.open(path, "wb") as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(RATE)
        out.writeframes(b"".join(struct.pack("<h", int(s * scale * 32767)) for s in samples))


def main():
    out_dir = sys.argv[1] if len(sys.argv) > 1 else os.path.join(
        os.path.dirname(__file__), "..", "core", "audio", "src", "main", "res", "raw")
    os.makedirs(out_dir, exist_ok=True)

    # E5 then A5: a light rising "ding-ding"
    correct = sequence(note(659.25, 0.12), note(880.0, 0.22), gap=0.04)
    # Two low, slightly detuned falling notes: a soft "bonk"
    soft = ((1, 1.0), (2, 0.15))
    wrong = sequence(note(220.0, 0.12, harmonics=soft, detune=3.0), note(174.6, 0.2, harmonics=soft, detune=3.0), gap=0.02)
    # C5 E5 G5 C6 arpeggio
    complete = sequence(
        note(523.25, 0.16), note(659.25, 0.16), note(783.99, 0.16), note(1046.5, 0.45),
        gap=0.05,
    )

    write(os.path.join(out_dir, "sfx_correct.wav"), correct)
    write(os.path.join(out_dir, "sfx_wrong.wav"), wrong)
    write(os.path.join(out_dir, "sfx_complete.wav"), complete)


if __name__ == "__main__":
    main()
