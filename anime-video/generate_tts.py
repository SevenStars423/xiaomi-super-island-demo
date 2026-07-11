#!/usr/bin/env python3
"""Generate Chinese TTS narration for all 6 scenes using kokoro-onnx."""
import os
import sys
import soundfile as sf
from kokoro_onnx import Kokoro

MODEL = os.path.expanduser("~/.cache/hyperframes/tts/kokoro-v1.0.int8.onnx")
VOICES = os.path.expanduser("~/.cache/hyperframes/tts/voices-v1.0.bin")

# Scene scripts: (text, voice, speed, output_file)
# zm_yunjian = young Chinese male (protagonist/narrator)
# zm_yunyang = Chinese male (old man 李老)
SCENES = [
    (
        "二十八岁，没工作，没存款，没女友。银行卡，只剩二十三块八。裁员短信炸进来，女友秒提分手，房东涨价五成。深夜独自煮泡面，连个问我饿不饿的人，都没有。",
        "zm_yunjian", 0.95, "scene1.wav",
    ),
    (
        "他想起做厨师的爷爷，祖传秘制乳鸽手艺。最后一搏！冲向菜市场，二十八块八，能买什么？一只乳鸽！爷爷，我信你一次！乳鸽！给我一只乳鸽！",
        "zm_yunjian", 1.0, "scene2.wav",
    ),
    (
        "白发老头突然出现，精准说出他三天的遭遇。塞来一本泛黄古籍，天食录。子时按书做乳鸽，你的人生，今晚改变。老头说完，就消失了。",
        "zm_yunjian", 0.95, "scene3.wav",
    ),
    (
        "草药摊角落，一株叶脉闪着银光的草，星辰草！心脏狂跳，古籍上的图案，和这株草一模一样。十块钱！这株草，我要了！",
        "zm_yunjian", 1.05, "scene4.wav",
    ),
    (
        "子时，油锅翻滚，星辰草研磨入锅。金银光芒，冲天而起，整间屋子被照成白昼。金光！真的是金光！这不是乳鸽，这是仙丹！",
        "zm_yunjian", 1.05, "scene5.wav",
    ),
    (
        "一口咬下，热流炸开全身，暗色毒素从毛孔狂涌而出。脱胎换骨！单手举起冰箱，一拳捏裂洗手台。这力量，是真的！老子再也不是废柴了！以后，我要保护一个人。",
        "zm_yunjian", 1.05, "scene6.wav",
    ),
]

def main():
    out_dir = os.path.dirname(os.path.abspath(__file__))
    print(f"Loading Kokoro model: {MODEL}")
    kokoro = Kokoro(MODEL, VOICES)
    print("Model loaded.\n")

    for i, (text, voice, speed, fname) in enumerate(SCENES, 1):
        out_path = os.path.join(out_dir, fname)
        print(f"Scene {i}: voice={voice} speed={speed}")
        print(f"  text: {text[:40]}...")
        samples, sample_rate = kokoro.create(
            text, voice=voice, speed=speed, lang="cmn"
        )
        sf.write(out_path, samples, sample_rate)
        duration = len(samples) / sample_rate
        print(f"  -> {fname} ({duration:.1f}s)\n")

    print("All scenes generated.")

if __name__ == "__main__":
    main()
