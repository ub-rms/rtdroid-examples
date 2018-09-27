#include <fivmr.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <limits.h>
#include <fcntl.h>
#include "fivmr_pcm.h"
int device_setup=0, device_rec_setup=0;
struct pcm *output_stream, *input_stream;
char *rec_buffer;
void config_write();
void config_read();
int buffer_size, rec_buffer_size;

void pcmNativeOpen(char* string_param){
  printf("PCM native open print %s\n", string_param);
  config_write();
  device_setup = 1;
  //LOG(0, ("Native Print %s", string_param));
}
void pcmNativeRecOpen(char *string_param){
  printf("PCM Native read open %s\n", string_param);
  config_read();
}
void pcmNativeClose(){
  printf("Native Audio Session close\n");
  device_setup = 0;
  system(mixer_playback_disable);
}
void pcmNativeCloseRec(){
  free(rec_buffer);
  system(mixer_record_disable);
}
void pcmNativeWrite(char *buffer){
  if(!device_setup){
    config_write();
    //Setup playback for first time
    device_setup = 1;
  }
#ifdef NATIVE_AUDIO
  if(output_stream!=NULL){
    pcm_write(output_stream, buffer, buffer_size);
  }
#endif 
}
char* pcmNativeRead(){
  if(!device_rec_setup){
    config_read();
    device_rec_setup = 1;
  }
#ifdef NATIVE_AUDIO
  if(input_stream!=NULL){
    int read = pcm_read(input_stream, rec_buffer, rec_buffer_size);
//    printf("%d bytes read\n", read);
  }
#endif
  return rec_buffer;
}
void config_write(){
  struct pcm_config capture;
  unsigned int flags = PCM_OUT;
  int errno_, i;
#ifdef NATIVE_AUDIO
  system(mixer_playback);
  output_stream = pcm_open(CARD_NUMBER, DEVICE_NUMBER_PLAY, flags, &pcm_config_audio_playback);
  buffer_size = pcm_frames_to_bytes(output_stream, pcm_get_buffer_size(output_stream));
  buffer_size = buffer_size/2;
  printf("%d buffer size %d\n", buffer_size, pcm_get_buffer_size(output_stream));
#endif
}
void config_read(){
#ifdef NATIVE_AUDIO
  system(mixer_record);
  input_stream = pcm_open(CARD_NUMBER, DEVICE_NUMBER_CAPTURE, PCM_IN, &pcm_config_audio_capture);
//  rec_buffer_size = pcm_frames_to_bytes(input_stream, pcm_get_buffer_size(input_stream));
  rec_buffer_size = pcm_get_buffer_size(input_stream);
  rec_buffer = malloc(rec_buffer_size);
  printf("Record %d buffer size %d\n", rec_buffer_size, pcm_get_buffer_size(input_stream));
#endif
  device_rec_setup = 1;
}
