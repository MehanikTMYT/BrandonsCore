layout(std140) uniform BCParams {
    mat4 ModelMat;
    vec4 BaseColor;
    ivec2 UV1Override;
    ivec2 UV2Override;
    float Time;
    float Decay;
    float Transition;
    float Hue;
    float Yaw;
    float Pitch;
    float Alpha;
    int SimpleLightFlag;
    int DisableLightFlag;
    int DisableOverlayFlag;
    float charge;
    float BCPadding;
    ivec2 ePos;
    ivec2 eSize;
    ivec2 screenSize;
};
#define SimpleLight (SimpleLightFlag != 0)
#define DisableLight (DisableLightFlag != 0)
#define DisableOverlay (DisableOverlayFlag != 0)
#define time Time
