package com.mentality.fabricwebmap.commands;
final class FullRenderCenter { private FullRenderCenter() {} static int tileForBlock(int block) { return Math.floorDiv(block, 256); } }
