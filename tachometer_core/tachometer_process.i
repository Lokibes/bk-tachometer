/* File : tachometer_process.i */
%module tachometer_process

%inline %{
extern void* Tachometer_Create();
extern int32_t Tachometer_Init(void* tacho);
extern int32_t Tachometer_Free(void* tacho);
extern int32_t Tachometer_Config(void* tacho, int32_t estimatedFreq);
extern int32_t Tachometer_Get_Audio_Frame_Location(void* tacho, int16_t** audioFrame);
extern int32_t Tachometer_Push(void* tacho, int16_t* inAudio, int32_t size);
extern float Tachometer_Process(void* tacho, int16_t* inAudio);
extern float* Tachometer_FFT_Out(void* tacho);
%}