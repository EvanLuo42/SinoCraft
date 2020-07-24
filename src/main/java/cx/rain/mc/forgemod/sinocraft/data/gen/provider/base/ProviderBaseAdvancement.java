package cx.rain.mc.forgemod.sinocraft.data.gen.provider.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cx.rain.mc.forgemod.sinocraft.utility.ProtectedHelper;
import net.minecraft.advancements.*;
import net.minecraft.command.FunctionObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class ProviderBaseAdvancement implements IDataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private DataGenerator generator;
    protected final Map<ResourceLocation, Advancement.Builder> Advancements = new HashMap<>();

    public ProviderBaseAdvancement(DataGenerator generatorIn) {
        generator = generatorIn;
    }

    protected static class AdvancementToJson{
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().registerTypeAdapter(Advancement.class, new TypeAdapter<Advancement>() {
            @Override
            public void write(JsonWriter out, Advancement value) throws IOException{
                out.beginObject();
                writeDisplay(out,value);
                if(value.getParent()!=null){
                    out.name("parent").value(value.getParent().getId().toString());
                }
                writeRewards(out,value);
                out.endObject();
            }

            //complete
            public void writeDisplay(JsonWriter out, Advancement adv) throws IOException {
                DisplayInfo info = adv.getDisplay();
                out.name("display").beginObject();
                    out.name("icon").beginObject();
                        out.name("item").value(info.getIcon().getItem().getRegistryName().toString());
                    out.endObject();
                    out.name("title").beginObject();
                           out.name("translate").value(info.getTitle().getString());
                    out.endObject();
                    out.name("description").beginObject();
                            out.name("translate").value(info.getDescription().getString());
                    out.endObject();
                    out.name("frame").value(info.getFrame().getName());
                    if(info.getBackground()!=null){
                        out.name("background").value(info.getBackground().toString());
                    }
                    out.name("show_toast").value(info.shouldShowToast());
                    out.name("announce_to_chat").value(info.shouldAnnounceToChat());
                    out.name("hidden").value(info.isHidden());
                out.endObject();
            }

            //incomplete
            public void writeCriteria(JsonWriter out,Advancement adv) throws IOException{

            }

            //complete
            public void writeRewards(JsonWriter out,Advancement adv) throws IOException {
                AdvancementRewards rewards = adv.getRewards();
                int experience = (int)ProtectedHelper.getField(AdvancementRewards.class,rewards,"experience");
                ResourceLocation[] recipes = (ResourceLocation[]) ProtectedHelper.getField(AdvancementRewards.class,rewards,"recipes");
                ResourceLocation[] loots = (ResourceLocation[]) ProtectedHelper.getField(AdvancementRewards.class,rewards,"loot");
                FunctionObject.CacheableFunction function =  (FunctionObject.CacheableFunction)ProtectedHelper.getField(AdvancementRewards.class,rewards,"function");
                out.name("rewards").beginObject();
                    if(experience!=0){
                        out.name("experience").value(experience);
                    }
                    if(recipes.length!=0){
                        out.name("recipes").beginArray();
                        for(ResourceLocation recipe : recipes){
                            out.value(recipe.toString());
                        }
                        out.endArray();
                    }
                    if(loots.length!=0){
                        out.name("loot").beginArray();
                        for(ResourceLocation loot : loots){
                            out.value(loot.toString());
                        }
                        out.endArray();
                    }
                    if(function!=null){
                        if(function.getId()!=null){
                            out.name("function").value(function.getId().toString());
                        }
                    }
                out.endObject();
            }

            @Override
            public Advancement read(JsonReader in) {
                return null;
            }
        }).create();

        public static JsonElement toJsonTree(Advancement advancement){
            return GSON.toJsonTree(advancement);
        }
    }

    protected abstract void registerAdvancements();

    @Override
    public void act(DirectoryCache directoryCache){
        registerAdvancements();

        Map<ResourceLocation, Advancement> advancements = new HashMap<>();
        for (Map.Entry<ResourceLocation, Advancement.Builder> entry : Advancements.entrySet()) {
            advancements.put(entry.getKey(), entry.getValue().build(entry.getKey()));
        }
        writeAdvancements(directoryCache, advancements);
    }

    private void writeAdvancements(DirectoryCache cache, Map<ResourceLocation, Advancement> tables) {
        Path outputFolder = generator.getOutputFolder();
        tables.forEach((key, advancement) -> {
            Path path = getPath(outputFolder, key);
            try {
                IDataProvider.save(GSON, cache, AdvancementToJson.toJsonTree(advancement), path);
            } catch (IOException e) {
                LOGGER.error("Couldn't write advancements {}", path, e);
            }
        });
    }

    protected static class CriterionInfo{
        public String name;
        public Criterion criterion;

        public CriterionInfo(String nameIn,Criterion criterionIn){
            name=nameIn;
            criterion=criterionIn;
        }
    }

    private static Path getPath(Path pathIn, ResourceLocation id) {
        return pathIn.resolve("data/" + id.getNamespace() + "/advancements/" + id.getPath() + ".json");
    }

    protected Advancement.Builder RootAdvancement(ItemStack icon, ITextComponent title, ITextComponent description,
                                                  @Nullable ResourceLocation background, FrameType frame, boolean showToast,
                                                  boolean announceToChat, boolean hidden, AdvancementRewards.Builder rewardsBuilder){
        return Advancement.Builder.builder()
                .withDisplay(
                        new DisplayInfo(icon,title,description,background,frame,showToast,announceToChat,hidden)
                )
                .withRewards(rewardsBuilder);
    }

    protected Advancement.Builder RootAdvancement(ItemStack icon, String title, String description,
                                                  @Nullable ResourceLocation background, FrameType frame, boolean showToast,
                                                  boolean announceToChat, boolean hidden, AdvancementRewards.Builder rewardsBuilder
                                                  ){
        return Advancement.Builder.builder()
                .withDisplay(
                        new DisplayInfo(icon,new StringTextComponent(title),new StringTextComponent(description),background,frame,showToast,announceToChat,hidden)
                )
                .withRewards(rewardsBuilder);
    }
}