// Copyright 2019 Citra MMJ Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

#pragma once

#include <string>

#include "common/common_types.h"

namespace Layout {
    struct FramebufferLayout;
}

namespace OSD {

enum class MessageType {
    FPS,
    Typeless,
    HWShader,
    CPUJit,
    New3DS,
    FMVHack,
    TexLoadHack,
};

namespace Color {
    constexpr u32 CYAN = 0xFF00FFFF;
    constexpr u32 GREEN = 0xFF00FF00;
    constexpr u32 RED = 0xFFFF0000;
    constexpr u32 BLUE = 0xFF00FFFF;
    constexpr u32 YELLOW = 0xFFFFFF30;
}; // namespace Color

namespace Duration {
    constexpr u32 SHORT = 2000;
    constexpr u32 NORMAL = 5000;
    constexpr u32 VERY_LONG = 10000;
    constexpr u32 FOREVER = -1;
}; // namespace Duration

void Initialize();

void AddMessage(const std::string &message, MessageType type, u32 duration, u32 color);

void DrawMessage(const Layout::FramebufferLayout& layout);

void Shutdown();

} // namespace OSD
