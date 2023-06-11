path.sh中KALDI_ROOT为kaldi的根目录，默认为和xvector文件夹同级，移动到其他位置需要更改KALDI_ROOT

generate_speaker.py生成用于提取说话人x-vector的配置文件(包含音频路径以及对应的用户姓名)，使用方法如下
python generate_speaker.py voice1.wav 1.txt

enroll.sh提取用户注册音频x-vector，会在data文件夹下生成用户的注册信息，使用方法如下
./enroll.sh 1.txt 2 username（1.txt为generate_speaker.py生成的配置文件）

auth.sh提取用户认证音频x-vector，会在data文件夹下生成用户的注册信息，使用方法如下
./enroll.sh 1.txt 2 username

score.sh计算用户的认证分数，会在data/username下生成用户的分数，使用方法如下
./score.sh username
