#!/bin/bash
# Generate Chinese TTS with espeak-ng for all 6 scenes
# Speed 240 wpm to fit ~15s per scene
cd /workspace/anime-video

espeak-ng -v cmn -s 240 -p 50 -a 200 -w scene1.wav "二十八岁，没工作没存款没女友。银行卡只剩二十三块八。深夜独自煮泡面，连个说话的人都没有。" 2>&1
espeak-ng -v cmn -s 240 -p 50 -a 200 -w scene2.wav "想起做厨师的爷爷，祖传秘制乳鸽。冲向菜市场，二十八块八买一只乳鸽。命运从此反转。" 2>&1
espeak-ng -v cmn -s 240 -p 50 -a 200 -w scene3.wav "白发老头出现，塞来泛黄古籍天食录。子时按书做乳鸽，你的人生今晚改变。说完就消失。" 2>&1
espeak-ng -v cmn -s 240 -p 50 -a 200 -w scene4.wav "草药摊角落，叶脉闪着银光的星辰草。和古籍图案一模一样。十块钱，这株草我要了。" 2>&1
espeak-ng -v cmn -s 240 -p 50 -a 200 -w scene5.wav "子时油锅翻滚，星辰草入锅。金银光芒冲天而起，屋子照成白昼。这不是乳鸽，是仙丹。" 2>&1
espeak-ng -v cmn -s 240 -p 50 -a 200 -w scene6.wav "一口咬下，热流炸开全身，脱胎换骨。单手举冰箱，一拳裂洗手台。再也不是废柴了。" 2>&1

echo "=== DURATIONS ==="
for i in 1 2 3 4 5 6; do
  python3 -c "import wave; w=wave.open('scene${i}.wav'); print(f'scene${i}.wav: {w.getnframes()/w.getframerate():.1f}s')" 2>&1
done
