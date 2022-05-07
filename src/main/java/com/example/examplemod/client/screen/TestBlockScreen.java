package com.example.examplemod.client.screen;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.client.widget.MultiLineTextFieldWidget;
import com.example.examplemod.container.TestBlockContainer;
import com.example.examplemod.network.PacketHandler;
import com.example.examplemod.network.packet.PrintVariableCard;
import com.example.examplemod.network.packet.UpdateInstructions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;

public class TestBlockScreen extends ContainerScreen<TestBlockContainer> {
    public static final int TEXTFIELD_X_COORDINATE = 9;
    public static final int TEXTFIELD_Y_COORDINATE = 9;
    public static final int TEXTFIELD_WIDTH = 158;
    public static final int TEXTFIELD_HEIGHT = 46;

    public static final int BUTTON_X_COORDINATE = 114;
    public static final int BUTTON_Y_COORDINATE = 38;

    private final ResourceLocation GUI = new ResourceLocation(ExampleMod.MOD_ID,
            "textures/gui/test_block_gui.png");
    private int updateCount;
    private long lastClickTime;
    private int lastClickIndex = -1;
    private int shownRowsStart = 0;
    //private TextInputUtil textInput;
    //private String instructions;
    //private TextDisplay displayCache;

    MultiLineTextFieldWidget textField;

    public TestBlockScreen(TestBlockContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);

//        this.textInput = new TextInputUtil(
//                () -> { return this.instructions; },
//                text -> {
//                    this.instructions = text;
//                    //this.displayCache = new TextDisplay(text);
//                    PacketHandler.INSTANCE.sendToServer(new UpdateInstructions(text));
//                    clearDisplayCache();
//                },
//                TextInputUtil.createClipboardGetter(this.minecraft),
//                TextInputUtil.createClipboardSetter(this.minecraft),
//                text -> { return text.length() < 1024; });
//
//
//        this.displayCache = getDisplayCache();

//        int TEXTFIELD_X_COORDINATE = 0;
//        int TEXTFIELD_Y_COORDINATE = 0;
//        int TEXTFIELD_WIDTH = 0;
//        int TEXTFIELD_HEIGHT = 0;

//        textField = new TextFieldWidget(font,
//                TEXTFIELD_X_COORDINATE + this.guiLeft,
//                TEXTFIELD_Y_COORDINATE + this.guiTop,
//                TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT,
//                new TranslationTextComponent("test"));
//        textField.setEnableBackgroundDrawing(false);
//        textField.setVisible(true);
//        textField.setText(this.container.instructions);
//        textField.setFocused2(true);
//        textField.setResponder(text -> {
//            PacketHandler.INSTANCE.sendToServer(new UpdateInstructions(text));
//            ExampleMod.LOGGER.info(text);
//        });
//
//        this.children.add(textField);
//        this.setFocusedDefault(textField);

        this.textField = new MultiLineTextFieldWidget(font,
                TEXTFIELD_X_COORDINATE,
                TEXTFIELD_Y_COORDINATE,
                TEXTFIELD_WIDTH,
                TEXTFIELD_HEIGHT,
                new TranslationTextComponent("testBlock.textField"));
        this.textField.setVisible(true);
        this.textField.setMaxLength(32500);
        this.textField.setValue(super.menu.instructions);
        textField.setResponder(text -> {
            PacketHandler.INSTANCE.sendToServer(new UpdateInstructions(text));
            ExampleMod.LOGGER.info(text);
        });
        this.addWidget(textField);

        Button button = new Button(
                BUTTON_X_COORDINATE + this.leftPos,
                BUTTON_Y_COORDINATE + this.topPos,
                100, 12, new TranslationTextComponent("press.me"),
                btn -> {
                    PacketHandler.INSTANCE.sendToServer(new PrintVariableCard());
                });
        button.visible = true;
        button.active = true;
        this.addButton(button);

//        this.testList = new TestList(this.minecraft, 50, 200, 5, 205, 15);
//        this.testList.setRenderBackground(false);
//        this.testList.setRenderSelection(false);
////        this.testList.setRenderTopAndBottom(false);
//        this.testList.setLeftPos(5);
//        this.children.add(this.testList);
//        this.setFocused(this.testList);
    }

//    @Override
//    public boolean charTyped(char codePoint, int modifiers) {
//        this.textInput.charTyped(codePoint);
//        return true;
//    }

//    @Override
//    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//        if (keyPressedInInput(keyCode, scanCode, modifiers)) {
//            clearDisplayCache();
//            return true;
//        }
//        return super.keyPressed(keyCode, scanCode, modifiers);
//    }

//    private boolean keyPressedInInput(int keyCode, int scanCode, int modifiers) {
//        InputMappings.Input mouseKey = InputMappings.getKey(keyCode, scanCode);
//        if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) { return true;}
//        switch (keyCode) {
//            case GLFW.GLFW_KEY_ENTER:
//            case GLFW.GLFW_KEY_KP_ENTER:
//                textInput.insertText("\n");
//                return true;
//            case GLFW.GLFW_KEY_DOWN:
//                keyDown();
//                return true;
//            case GLFW.GLFW_KEY_UP:
//                keyUp();
//                return true;
//            case GLFW.GLFW_KEY_HOME:
//                keyHome();
//                return true;
//            case GLFW.GLFW_KEY_END:
//                keyEnd();
//                return true;
//            default: return this.textInput.keyPressed(keyCode);
//        }
//    }

//    private void keyUp() {
//        this.changeLine(-1);
//    }
//
//    private void keyDown() {
//        this.changeLine(1);
//    }

//    private void changeLine(int lineDelta) {
//        int cursorIndex = this.textInput.getCursorPos();
//        int newIndex = this.getDisplayCache().changeLine(cursorIndex, lineDelta);
//        this.textInput.setCursorPos(newIndex, Screen.hasShiftDown());
//    }
//
//    private void keyHome() {
//        int cursorIndex = this.textInput.getCursorPos();
//        int lineStartIndex = this.getDisplayCache().findLineStart(cursorIndex);
//        this.textInput.setCursorPos(lineStartIndex, Screen.hasShiftDown());
//    }
//
//    private void keyEnd() {
//        int cursorIndex = this.textInput.getCursorPos();
//        int lineEndIndex = this.getDisplayCache().findLineEnd(cursorIndex);
//        this.textInput.setCursorPos(lineEndIndex, Screen.hasShiftDown());
//    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        super.removed();
    }

    @Override
    public void tick() {
        super.tick();
        this.updateCount++;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        //this.testList.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);

//        this.textField.setFocused2(true);
//        this.textField.render(matrixStack, mouseX, mouseY, partialTicks);


//        this.font.drawText(matrixStack, new StringTextComponent(instructions),
//                TEXTFIELD_X_COORDINATE + this.guiLeft,
//                TEXTFIELD_Y_COORDINATE + this.guiTop,
//                14737632);
        /*this.font.drawString(matrixStack, instructions,
                TEXTFIELD_X_COORDINATE + this.guiLeft,
                TEXTFIELD_Y_COORDINATE + this.guiTop,
                14737632);

        drawPositionIndicator(matrixStack, true,
                25,
                25,
                14737632);*/

        /*for (int i = 0; i < displayCache.inputList.size(); i++) {
            this.font.drawString(matrixStack, displayCache.inputList.get(i),
                    TEXTFIELD_X_COORDINATE + this.guiLeft,
                    TEXTFIELD_Y_COORDINATE + this.guiTop + i * font.FONT_HEIGHT,
                    14737632);
        }*/

//        TextDisplay textDisplay = this.getDisplayCache();
//
//        for(TextLine textLine : textDisplay.lines) {
//            this.font.draw(matrixStack, textLine.textComponent,
//                    (float)textLine.x, (float)textLine.y, 0xE0E0E0);
//        }
//
//        this.renderHighlight(textDisplay.selection);
//        this.renderCursor(matrixStack, textDisplay.cursor, textDisplay.cursorAtEnd);

        for(Widget button : buttons){
            button.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        textField.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        this.minecraft.getTextureManager().bind(GUI);
        int i = this.leftPos;
        int j = this.topPos;
        this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

//    private void renderCursor(MatrixStack matrixStack, Point cursor, boolean endOfString) {
//        if (this.updateCount / 6 % 2 == 0) {
//            cursor = this.convertLocalToScreen(cursor);
//            if (!endOfString) {
//                AbstractGui.fill(matrixStack,
//                        cursor.x, cursor.y - 1,
//                        cursor.x + 1, cursor.y + 9,
//                        0xFFE0E0E0);
//            } else {
//                this.font.draw(matrixStack, "_",
//                        (float)cursor.x, (float)cursor.y, 0xE0E0E0);
//            }
//        }
//
//    }

    private void renderHighlight(Rectangle2d[] selection) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);

        for(Rectangle2d rectangle2d : selection) {
            int x1 = rectangle2d.getX();
            int y1 = rectangle2d.getY();
            int x2 = x1 + rectangle2d.getWidth();
            int y2 = y1 + rectangle2d.getHeight();
            bufferbuilder.vertex((double)x1, (double)y2, 0.0D).endVertex();
            bufferbuilder.vertex((double)x2, (double)y2, 0.0D).endVertex();
            bufferbuilder.vertex((double)x2, (double)y1, 0.0D).endVertex();
            bufferbuilder.vertex((double)x1, (double)y1, 0.0D).endVertex();
        }

        tessellator.end();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

//    private Point convertScreenToLocal(Point point) {
////        return new Point(
////                point.x - (this.width - 192) / 2 - 36,
////                point.y - 32);
//        return new Point(
//                point.x - this.leftPos - TEXTFIELD_X_COORDINATE,
//                point.y - this.topPos - TEXTFIELD_Y_COORDINATE);
//    }
//
//    private Point convertLocalToScreen(Point point) {
////        return new Point(
////                point.x + (this.width - 192) / 2 + 36,
////                point.y + 32);
//        return new Point(
//                point.x + this.leftPos + TEXTFIELD_X_COORDINATE,
//                point.y + this.topPos + TEXTFIELD_Y_COORDINATE);
//    }

//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        if (mouseX > this.leftPos + TEXTFIELD_X_COORDINATE
//                && mouseX < this.leftPos + TEXTFIELD_X_COORDINATE + TEXTFIELD_WIDTH
//                && mouseY > this.topPos + TEXTFIELD_Y_COORDINATE
//                && mouseY < this.topPos + TEXTFIELD_Y_COORDINATE + TEXTFIELD_HEIGHT ) {
//            if (button == 0) {
//                long i = Util.getMillis();
//                TextDisplay textDisplay = this.getDisplayCache();
//                int textIndex = textDisplay.getIndexAtPosition(this.font,
//                        this.convertScreenToLocal(new Point((int) mouseX, (int) mouseY)));
//                if (textIndex >= 0) {
//                    if (textIndex == this.lastClickIndex && i - this.lastClickTime < 250L) {
//                        if (!this.textInput.isSelecting()) {
//                            this.selectWord(textIndex);
//                        } else {
//                            this.textInput.selectAll();
//                        }
//                    } else {
//                        this.textInput.setCursorPos(textIndex, Screen.hasShiftDown());
//                    }
//
//                    this.clearDisplayCache();
//                }
//
//                this.lastClickIndex = textIndex;
//                this.lastClickTime = i;
//            }
//        }
//        else {
//            super.mouseClicked(mouseX, mouseY, button);
//        }
//        return true;
//    }

//    private void selectWord(int i) {
//        String s = this.instructions;
//        this.textInput.setSelectionRange(
//                CharacterManager.getWordPosition/*getWordPosition*/(
//                        s, -1, i, false),
//                CharacterManager.getWordPosition/*getWordPosition*/(
//                        s, 1, i, false));
//    }

//    @Override
//    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
//        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
//            return true;
//        } else {
//            if (button == 0) {
//                TextDisplay textDisplay = this.getDisplayCache();
//                int i = textDisplay.getIndexAtPosition(this.font, this.convertScreenToLocal(
//                        new Point((int)mouseX, (int)mouseY)));
//                this.textInput.setCursorPos(i, true);
//                this.clearDisplayCache();
//            }
//
//            return true;
//        }
//    }

//    private TextDisplay getDisplayCache() {
//        if (this.displayCache == null) {
//            this.displayCache = this.rebuildDisplayCache();
//        }
//
//        return this.displayCache;
//    }
//
//    private void clearDisplayCache() {
//        this.displayCache = null;
//    }

//    private TextDisplay rebuildDisplayCache() {
//        String fullText = this.instructions;
//        if (fullText.isEmpty()) {
//            return TextDisplay.EMPTY;
//        } else {
//            int cursorIndex = this.textInput.getCursorPos();
//            int selectionStartIndex = this.textInput.getSelectionPos();
//            IntList intlist = new IntArrayList();
//            List<TextLine> lines = Lists.newArrayList();
//            MutableInt mutableint = new MutableInt();
//            MutableBoolean mutableboolean = new MutableBoolean();
//            CharacterManager charactermanager = this.font.getSplitter();
//            charactermanager.splitLines/*splitLines*/(fullText, /*maxWidth*/ TEXTFIELD_WIDTH, Style.EMPTY, true,
//                    (style, sliceStart, sliceEnd) -> {
//                        int line = mutableint.getAndIncrement();
//                        String textSlice = fullText.substring(sliceStart, sliceEnd);
//                        mutableboolean.setValue(textSlice.endsWith("\n"));
//                        String textSliceFinal = StringUtils.stripEnd(textSlice, " \n");
//                        int y = line * 9;
//                        Point linePoint = this.convertLocalToScreen(new Point(0, y));
//                        intlist.add(sliceStart);
//                        lines.add(new TextLine(style, textSliceFinal, linePoint.x, linePoint.y));
//                    });
//            int[] lineStarts = intlist.toIntArray();
//            boolean cursorAtEnd = cursorIndex == fullText.length();
//            Point cursor;
//            if (cursorAtEnd && mutableboolean.isTrue()) {
//                cursor = new Point(0, lines.size() * 9);
//            } else {
//                int line = findLineFromPos(lineStarts, cursorIndex);
//                int cursorLineIndex = this.font.width(fullText.substring(lineStarts[line], cursorIndex));
//                cursor = new Point(cursorLineIndex, line * 9);
//            }
//
//            List<Rectangle2d> selection = Lists.newArrayList();
//            if (cursorIndex != selectionStartIndex) {
//                int selectionStart = Math.min(cursorIndex, selectionStartIndex);
//                int selectionEnd = Math.max(cursorIndex, selectionStartIndex);
//                int selectionStartLine = findLineFromPos(lineStarts, selectionStart);
//                int selectionEndLine = findLineFromPos(lineStarts, selectionEnd);
//                if (selectionStartLine == selectionEndLine) {
//                    int selectionStartY = selectionStartLine * 9;
//                    int lineStartIndex = lineStarts[selectionStartLine];
//                    selection.add(this.createPartialLineSelection(fullText, charactermanager,
//                            selectionStart, selectionEnd, selectionStartY, lineStartIndex));
//                } else {
//                    int firstLineEndIndex = selectionStartLine + 1 > lineStarts.length
//                            ? fullText.length()
//                            : lineStarts[selectionStartLine + 1];
//                    selection.add(this.createPartialLineSelection(fullText, charactermanager,
//                            selectionStart, firstLineEndIndex, selectionStartLine * 9, lineStarts[selectionStartLine]));
//
//                    for(int line = selectionStartLine + 1; line < selectionEndLine; ++line) {
//                        int y = line * 9;
//                        String lineText = fullText.substring(lineStarts[line], lineStarts[line + 1]);
//                        int endX = (int)charactermanager.stringWidth/*stringWidth*/(lineText);
//                        selection.add(this.createSelection(new Point(0, y), new Point(endX, y + 9)));
//                    }
//
//                    selection.add(this.createPartialLineSelection(fullText, charactermanager,
//                            lineStarts[selectionEndLine], selectionEnd,
//                            selectionEndLine * 9, lineStarts[selectionEndLine]));
//                }
//            }
//
//            return new TextDisplay(fullText, cursor, cursorAtEnd, lineStarts,
//                    lines.toArray(new TextLine[0]), selection.toArray(new Rectangle2d[0]));
//        }
//    }

//    private static int findLineFromPos(int[] intArray, int intIn) {
//        int i = Arrays.binarySearch(intArray, intIn);
//        return i < 0 ? -(i + 2) : i;
//    }
//
//    private Rectangle2d createPartialLineSelection(String text, CharacterManager characterManager,
//                                                   int selectionStart, int selectionEnd,
//                                                   int selectionStartY, int lineStartIndex) {
//        String testToStart = text.substring(lineStartIndex, selectionStart);
//        String textToEnd = text.substring(lineStartIndex, selectionEnd);
//        Point selectionStartPoint = new Point((int)characterManager
//                .stringWidth/*stringWidth*/(testToStart), selectionStartY);
//        Point selectionEndPoint = new Point((int)characterManager
//                .stringWidth/*stringWidth*/(textToEnd), selectionStartY + 9);
//        return this.createSelection(selectionStartPoint, selectionEndPoint);
//    }
//
//    private Rectangle2d createSelection(Point selectionStartPointIn, Point selectionEndPointIn) {
//        Point selectionStartPoint = this.convertLocalToScreen(selectionStartPointIn);
//        Point selectionEndPoint = this.convertLocalToScreen(selectionEndPointIn);
//        int xStart = Math.min(selectionStartPoint.x, selectionEndPoint.x);
//        int xEnd = Math.max(selectionStartPoint.x, selectionEndPoint.x);
//        int yStart = Math.min(selectionStartPoint.y, selectionEndPoint.y);
//        int yEnd = Math.max(selectionStartPoint.y, selectionEndPoint.y);
//        return new Rectangle2d(xStart, yStart, xEnd - xStart, yEnd - yStart);
//    }

//    static class TextLine {
//        private final Style style;
//        private final String string;
//        private final ITextComponent textComponent;
//        private final int x;
//        private final int y;
//
//        public TextLine(Style style, String string, int x, int y) {
//            this.style = style;
//            this.string = string;
//            this.x = x;
//            this.y = y;
//            this.textComponent = (new StringTextComponent(string)).setStyle(style);
//        }
//    }
//
//    static class TextDisplay {
//        private static final TextDisplay EMPTY =
//                new TextDisplay(
//                        "",
//                        new Point(0, 0),
//                        true,
//                        new int[]{0},
//                        new TextLine[]{
//                                new TextLine(
//                                        Style.EMPTY,
//                                        "",
//                                        0, 0)},
//                        new Rectangle2d[0]);
//        List<String> inputList = new ArrayList<String>();
//        private final String fullText;
//        private final Point cursor;
//        private final boolean cursorAtEnd;
//        private final int[] lineStarts;
//        private final TextLine[] lines;
//        private final Rectangle2d[] selection;
//
//        public TextDisplay(String fullText, Point cursor, Boolean cursorAtEnd,
//                           int[] lineStarts, TextLine[] lines, Rectangle2d[] selection) {
//            this.fullText = fullText;
//            this.cursor = cursor;
//            this.cursorAtEnd = cursorAtEnd;
//            this.lineStarts = lineStarts;
//            this.lines = lines;
//            this.selection = selection;
//        }
//
//        public int getIndexAtPosition(FontRenderer font, Point point) {
//            int i = point.y / 9;
//            if (i < 0) {
//                return 0;
//            } else if (i >= this.lines.length) {
//                return this.fullText.length();
//            } else {
//                TextLine textLine = this.lines[i];
//                return this.lineStarts[i] + font.getSplitter()
//                        .plainIndexAtWidth/*plainIndexAtWidth*/(textLine.string, point.x, textLine.style);
//            }
//        }
//
//        public int changeLine(int cursorIndex, int lineDelta) {
//            int line = findLineFromPos(this.lineStarts, cursorIndex);
//            int newLine = line + lineDelta;
//            int newCursorIndex;
//            if (newLine < 0) {
//                newCursorIndex = 0;
//            } else if (newLine < this.lineStarts.length) {
//                int cursorLineIndex = cursorIndex - this.lineStarts[line];
//                int newLineEndIndex = this.lines[newLine].string.length();
//                newCursorIndex = this.lineStarts[newLine] + Math.min(cursorLineIndex, newLineEndIndex);
//            } else {
//                newCursorIndex = this.fullText.length();
//            }
//
//            return newCursorIndex;
//        }
//
//        public int findLineStart(int cursorIndex) {
//            int line = findLineFromPos(this.lineStarts, cursorIndex);
//            return this.lineStarts[line];
//        }
//
//        public int findLineEnd(int cursorIndex) {
//            int line = findLineFromPos(this.lineStarts, cursorIndex);
//            return this.lineStarts[line] + this.lines[line].string.length();
//        }
//    }
//
//    static class Point {
//        public final int x;
//        public final int y;
//
//        Point(int x, int y) {
//            this.x = x;
//            this.y = y;
//        }
//    }
}
