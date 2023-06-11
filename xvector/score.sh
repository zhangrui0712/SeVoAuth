. ./cmd.sh
. ./path.sh

username=$1

trials=data/$username/authenticate/auth.trials

local/produce_trials.py data/$username/authenticate/utt2spk $trials

mkdir -p data/$username/scores/log

# Get results using the adapted PLDA model.
$train_cmd data/$username/scores/log/scoring_adapt.log \
    ivector-plda-scoring --normalize-length=true \
    --num-utts=ark:data/$username/enroll/feat/xvectors_enroll_mfcc/num_utts.ark \
    "ivector-copy-plda --smoothing=0.0 exp/xvectors_train_combined/plda - |" \
    "ark:ivector-mean ark:data/$username/enroll/spk2utt scp:data/$username/enroll/feat/xvectors_enroll_mfcc/xvector.scp ark:- | ivector-subtract-global-mean exp/xvectors_train_combined/mean.vec ark:- ark:- | transform-vec exp/xvectors_train_combined/transform.mat ark:- ark:- | ivector-normalize-length ark:- ark:- |" \
    "ark:ivector-subtract-global-mean exp/xvectors_train_combined/mean.vec scp:data/$username/authenticate/feat/xvectors_test_mfcc/xvector.scp ark:- | transform-vec exp/xvectors_train_combined/transform.mat ark:- ark:- | ivector-normalize-length ark:- ark:- |" \
    "cat '$trials' | cut -d\  --fields=1,2 |" data/$username/scores/scores_adapt || exit 1;

# compute EER
#eer=$(/home/taotao/Downloads/kaldi/dist/bin/compute-eer <(python local/prepare_for_eer.py $trials data/scores/scores_adapt) 2>/dev/null)
#$eer >data/scores/results.txt
