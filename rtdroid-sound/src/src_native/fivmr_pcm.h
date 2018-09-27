#include<limits.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include "fivmr_asoundlib.h"

#define SAMPLE_RATE 44100
#define DEFAULT_CHANNEL_COUNT 2
#define AUDIO_CAPTURE_PERIOD_COUNT 2
#define AUDIO_PLAYBACK_PERIOD_COUNT 2
#define CARD_NUMBER 0
#define DEVICE_NUMBER_CAPTURE 0
#define DEVICE_NUMBER_PLAY 0

struct pcm_config pcm_config_audio_capture = {
  .channels = 1,
  .period_count = 4,
  .period_size = 1024,
  .format = PCM_FORMAT_S16_LE,
  .rate = SAMPLE_RATE, 
  .start_threshold = 0,
  .stop_threshold = INT_MAX,
  .avail_min = 0,
};
struct pcm_config pcm_config_audio_playback = {
  .channels = DEFAULT_CHANNEL_COUNT,
  .period_count = AUDIO_PLAYBACK_PERIOD_COUNT,
  .period_size = 240,
  .format = PCM_FORMAT_S16_LE,
  .rate = SAMPLE_RATE,
  .start_threshold = 0,
  .stop_threshold = INT_MAX,
  .avail_min = 0,
};

char mixer_record[] = "tinymix \"AIF1_CAP Mixer SLIM TX7\" 1\n \
                        tinymix \"DEC6 MUX\" \"ADC1\"\n \
                        tinymix \"SLIM TX7 MUX\" \"DEC6\"\n \
                        tinymix \"IIR1 INP1 MUX\" \"DEC6\"\n \
                        tinymix \"MultiMedia1 Mixer SLIM_0_TX\" 1\n \
                        tinymix \"TX6 HPF Switch\" 0\n \
                        tinymix \"ADC1 Volume\" 16\n \
                        tinymix \"DEC6 Volume\" 86\n";

char mixer_record_disable[] = "tinymix \"AIF1_CAP Mixer SLIM TX7\" 1\n \
                               tinymix \"DEC6 MUX\" \"ZERO\"        \n \
                               tinymix \"SLIM TX7 MUX\" \"ZERO\"\n \
                               tinymix \"IIR1 INP1 MUX\" \"ZERO\"\n \
                               tinymix \"MultiMedia1 Mixer SLIM_0_TX\" 0\n \
                               tinymix \"TX6 HPF Switch\" 1\n";
char mixer_playback[] = "tinymix \"SPK DRV Volume\" 8\n \
                         tinymix \"RX7 Digital Volume\" 94\n \
                         tinymix \"SLIMBUS_0_RX Audio Mixer MultiMedia1\" 1\n \
                         tinymix \"RX7 MIX1 INP1\" \"RX1\"\n \
                         tinymix \"SLIM RX1 MUX\" \"AIF1_PB\"\n \
                         tinymix \"SLIM_0_RX Channels\" \"One\"\n";

char mixer_playback_disable[] = "tinymix \"SLIMBUS_0_RX Audio Mixer MultiMedia1\" 0\n \
                                 tinymix \"RX7 MIX1 INP1\" \"ZERO\"\n \
                                 tinymix \"SLIM RX1 MUX\" \"AIF1_PB\"\n \
                                 tinymix \"SLIM_0_RX Channels\" \"One\"\n";
