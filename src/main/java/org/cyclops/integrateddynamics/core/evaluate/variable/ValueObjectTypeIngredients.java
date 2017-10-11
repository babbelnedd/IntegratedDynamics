package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.ToString;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.capability.recipehandler.*;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * Value type with values that are ingredients.
 * @author rubensworks
 */
public class ValueObjectTypeIngredients extends ValueObjectTypeBase<ValueObjectTypeIngredients.ValueIngredients> implements
        IValueTypeNamed<ValueObjectTypeIngredients.ValueIngredients>, IValueTypeNullable<ValueObjectTypeIngredients.ValueIngredients> {

    public ValueObjectTypeIngredients() {
        super("ingredients");
    }

    @Override
    public ValueIngredients getDefault() {
        return ValueIngredients.of(null);
    }

    @Override
    public String toCompactString(ValueIngredients value) {
        if (value.getRawValue().isPresent()) {
            return value.getRawValue().get().toString();
        }
        return "";
    }

    @Override
    public String serialize(ValueIngredients value) {
        if(!value.getRawValue().isPresent()) return "";

        NBTTagCompound tag = new NBTTagCompound();
        IIngredients ingredients = value.getRawValue().get();

        NBTTagList itemStacks = new NBTTagList();
        for (List<ValueObjectTypeItemStack.ValueItemStack> valueItemStacks : ingredients.getItemStacksRaw()) {
            NBTTagList list = new NBTTagList();
            for (ValueObjectTypeItemStack.ValueItemStack valueItemStack : valueItemStacks) {
                list.appendTag(new NBTTagString(ValueTypes.OBJECT_ITEMSTACK.serialize(valueItemStack)));
            }
            itemStacks.appendTag(list);
        }
        tag.setTag("items", itemStacks);

        NBTTagList fluidStacks = new NBTTagList();
        for (List<ValueObjectTypeFluidStack.ValueFluidStack> valueFluidStacks : ingredients.getFluidStacksRaw()) {
            NBTTagList list = new NBTTagList();
            for (ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack : valueFluidStacks) {
                list.appendTag(new NBTTagString(ValueTypes.OBJECT_FLUIDSTACK.serialize(valueFluidStack)));
            }
            fluidStacks.appendTag(list);
        }
        tag.setTag("fluids", fluidStacks);

        NBTTagList energies = new NBTTagList();
        for (List<ValueTypeInteger.ValueInteger> valueEnergies : ingredients.getEnergiesRaw()) {
            NBTTagList list = new NBTTagList();
            for (ValueTypeInteger.ValueInteger valueEnergy : valueEnergies) {
                list.appendTag(new NBTTagString(ValueTypes.INTEGER.serialize(valueEnergy)));
            }
            energies.appendTag(list);
        }
        tag.setTag("energies", energies);

        return tag.toString();
    }

    @Override
    public ValueIngredients deserialize(String value) {
        if(Strings.isNullOrEmpty(value)) return ValueIngredients.of(null);

        try {
            List<List<ValueObjectTypeItemStack.ValueItemStack>> itemStacks = Lists.newArrayList();
            List<List<ValueObjectTypeFluidStack.ValueFluidStack>> fluidStacks = Lists.newArrayList();
            List<List<ValueTypeInteger.ValueInteger>> energies = Lists.newArrayList();

            NBTTagCompound tag = JsonToNBT.getTagFromJson(value);

            for (NBTBase subTag : tag.getTagList("items", MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal())) {
                NBTTagList listTag = ((NBTTagList) subTag);
                List<ValueObjectTypeItemStack.ValueItemStack> list = Lists.newArrayList();
                itemStacks.add(list);
                for (int i = 0; i < listTag.tagCount(); i++) {
                    list.add(ValueTypes.OBJECT_ITEMSTACK.deserialize(listTag.getStringTagAt(i)));
                }
            }

            for (NBTBase subTag : tag.getTagList("fluids", MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal())) {
                NBTTagList listTag = ((NBTTagList) subTag);
                List<ValueObjectTypeFluidStack.ValueFluidStack> list = Lists.newArrayList();
                fluidStacks.add(list);
                for (int i = 0; i < listTag.tagCount(); i++) {
                    list.add(ValueTypes.OBJECT_FLUIDSTACK.deserialize(listTag.getStringTagAt(i)));
                }
            }

            for (NBTBase subTag : tag.getTagList("energies", MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal())) {
                NBTTagList listTag = ((NBTTagList) subTag);
                List<ValueTypeInteger.ValueInteger> list = Lists.newArrayList();
                energies.add(list);
                for (int i = 0; i < listTag.tagCount(); i++) {
                    list.add(ValueTypes.INTEGER.deserialize(listTag.getStringTagAt(i)));
                }
            }

            return ValueIngredients.of(new IngredientsRecipeLists(
                    itemStacks,
                    fluidStacks,
                    energies
            ));
        } catch (NBTException | RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Something went wrong while deserializing '%s'.", value));
        }
    }

    @Override
    public String getName(ValueIngredients a) {
        return toCompactString(a);
    }

    @Override
    public boolean isNull(ValueIngredients a) {
        return !a.getRawValue().isPresent();
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return null; // TODO
    }

    @ToString
    public static class ValueIngredients extends ValueOptionalBase<IIngredients> {

        private ValueIngredients(IIngredients recipe) {
            super(ValueTypes.OBJECT_INGREDIENTS, recipe);
        }

        public static ValueIngredients of(IIngredients recipe) {
            return new ValueIngredients(recipe);
        }

        @Override
        protected boolean isEqual(IIngredients a, IIngredients b) {
            return a.equals(b);
        }
    }

    public static interface IIngredients {

        public int getItemStackIngredients();
        public List<ValueObjectTypeItemStack.ValueItemStack> getItemStacks(int index);
        public Predicate<ValueObjectTypeItemStack.ValueItemStack> getItemStackPredicate(int index);
        public List<List<ValueObjectTypeItemStack.ValueItemStack>> getItemStacksRaw();

        public int getFluidStackIngredients();
        public List<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStacks(int index);
        public Predicate<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStackPredicate(int index);
        public List<List<ValueObjectTypeFluidStack.ValueFluidStack>> getFluidStacksRaw();

        public int getEnergyIngredients();
        public List<ValueTypeInteger.ValueInteger> getEnergies(int index);
        public Predicate<ValueTypeInteger.ValueInteger> getEnergiesPredicate(int index);
        public List<List<ValueTypeInteger.ValueInteger>> getEnergiesRaw();

    }

    public static class IngredientsRecipeIngredientsWrapper implements IIngredients {

        private final RecipeIngredients ingredients;

        public IngredientsRecipeIngredientsWrapper(RecipeIngredients ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public int getItemStackIngredients() {
            return ingredients.getIngredients(RecipeComponent.ITEMSTACK).size();
        }

        protected List<ValueObjectTypeItemStack.ValueItemStack> recipeIngredientItemStackToList(IRecipeIngredient<ItemStack, ItemHandlerRecipeTarget> input) {
            return Lists.transform(input.getMatchingInstances(), new Function<ItemStack, ValueObjectTypeItemStack.ValueItemStack>() {
                @Nullable
                @Override
                public ValueObjectTypeItemStack.ValueItemStack apply(ItemStack input) {
                    return ValueObjectTypeItemStack.ValueItemStack.of(input);
                }
            });
        }

        @Override
        public List<ValueObjectTypeItemStack.ValueItemStack> getItemStacks(int index) {
            return recipeIngredientItemStackToList(ingredients.getIngredients(RecipeComponent.ITEMSTACK).get(index));
        }

        @Override
        public Predicate<ValueObjectTypeItemStack.ValueItemStack> getItemStackPredicate(int index) {
            return valueItemStack -> ingredients.getIngredients(RecipeComponent.ITEMSTACK)
                    .get(index).test(valueItemStack.getRawValue());
        }

        @Override
        public List<List<ValueObjectTypeItemStack.ValueItemStack>> getItemStacksRaw() {
            return Lists.transform(ingredients.getIngredients(RecipeComponent.ITEMSTACK), new Function<IRecipeIngredient<ItemStack, ItemHandlerRecipeTarget>, List<ValueObjectTypeItemStack.ValueItemStack>>() {
                @Nullable
                @Override
                public List<ValueObjectTypeItemStack.ValueItemStack> apply(@Nullable IRecipeIngredient<ItemStack, ItemHandlerRecipeTarget> input) {
                    return recipeIngredientItemStackToList(input);
                }
            });
        }

        @Override
        public int getFluidStackIngredients() {
            return ingredients.getIngredients(RecipeComponent.FLUIDSTACK).size();
        }

        protected List<ValueObjectTypeFluidStack.ValueFluidStack> recipeIngredientFluidStackToList(IRecipeIngredient<FluidStack, FluidHandlerRecipeTarget> input) {
            return Lists.transform(input.getMatchingInstances(),
                    new Function<FluidStack, ValueObjectTypeFluidStack.ValueFluidStack>() {
                        @Nullable
                        @Override
                        public ValueObjectTypeFluidStack.ValueFluidStack apply(FluidStack input) {
                            return ValueObjectTypeFluidStack.ValueFluidStack.of(input);
                        }
                    });
        }

        @Override
        public List<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStacks(int index) {
            return recipeIngredientFluidStackToList(ingredients.getIngredients(RecipeComponent.FLUIDSTACK).get(index));
        }

        @Override
        public Predicate<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStackPredicate(int index) {
            return valueFluidStack -> ingredients.getIngredients(RecipeComponent.FLUIDSTACK)
                    .get(index).test(valueFluidStack.getRawValue().orNull());
        }

        @Override
        public List<List<ValueObjectTypeFluidStack.ValueFluidStack>> getFluidStacksRaw() {
            return Lists.transform(ingredients.getIngredients(RecipeComponent.FLUIDSTACK),
                    new Function<IRecipeIngredient<FluidStack, FluidHandlerRecipeTarget>, List<ValueObjectTypeFluidStack.ValueFluidStack>>() {
                        @Nullable
                        @Override
                        public List<ValueObjectTypeFluidStack.ValueFluidStack> apply(@Nullable IRecipeIngredient<FluidStack, FluidHandlerRecipeTarget> input) {
                            return recipeIngredientFluidStackToList(input);
                        }
                    });
        }

        @Override
        public int getEnergyIngredients() {
            return ingredients.getIngredients(RecipeComponent.ENERGY).size();
        }

        protected List<ValueTypeInteger.ValueInteger> recipeIngredientEnergyToList(IRecipeIngredient<Integer, IEnergyStorage> input) {
            return Lists.transform(input.getMatchingInstances(), new Function<Integer, ValueTypeInteger.ValueInteger>() {
                @Nullable
                @Override
                public ValueTypeInteger.ValueInteger apply(@Nullable Integer input) {
                    return ValueTypeInteger.ValueInteger.of(input);
                }
            });
        }

        @Override
        public List<ValueTypeInteger.ValueInteger> getEnergies(int index) {
            return recipeIngredientEnergyToList(ingredients.getIngredients(RecipeComponent.ENERGY).get(index));
        }

        @Override
        public Predicate<ValueTypeInteger.ValueInteger> getEnergiesPredicate(int index) {
            return valueInteger -> ingredients.getIngredients(RecipeComponent.ENERGY).get(index)
                    .test(valueInteger.getRawValue());
        }

        @Override
        public List<List<ValueTypeInteger.ValueInteger>> getEnergiesRaw() {
            return Lists.transform(ingredients.getIngredients(RecipeComponent.ENERGY),
                    new Function<IRecipeIngredient<Integer, IEnergyStorage>, List<ValueTypeInteger.ValueInteger>>() {
                        @Nullable
                        @Override
                        public List<ValueTypeInteger.ValueInteger> apply(@Nullable IRecipeIngredient<Integer, IEnergyStorage> input) {
                            return recipeIngredientEnergyToList(input);
                        }
                    });
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof IIngredients
                    && this.getItemStacksRaw().equals(((IIngredients) obj).getItemStacksRaw())
                    && this.getFluidStacksRaw().equals(((IIngredients) obj).getFluidStacksRaw())
                    && this.getEnergiesRaw().equals(((IIngredients) obj).getEnergiesRaw()));
        }

        @Override
        public String toString() {
            return ingredients.toString();
        }
    }

    public static class IngredientsRecipeLists implements IIngredients {

        private final List<List<ValueObjectTypeItemStack.ValueItemStack>> itemStacks;
        private final List<List<ValueObjectTypeFluidStack.ValueFluidStack>> fluidStacks;
        private final List<List<ValueTypeInteger.ValueInteger>> energies;

        public IngredientsRecipeLists(List<List<ValueObjectTypeItemStack.ValueItemStack>> itemStacks,
                                      List<List<ValueObjectTypeFluidStack.ValueFluidStack>> fluidStacks,
                                      List<List<ValueTypeInteger.ValueInteger>> energies) {
            this.itemStacks = itemStacks;
            this.fluidStacks = fluidStacks;
            this.energies = energies;
        }

        @Override
        public int getItemStackIngredients() {
            return itemStacks.size();
        }

        @Override
        public List<ValueObjectTypeItemStack.ValueItemStack> getItemStacks(int index) {
            return itemStacks.get(index);
        }

        @Override
        public Predicate<ValueObjectTypeItemStack.ValueItemStack> getItemStackPredicate(int index) {
            return itemStacks.get(index)::contains;
        }

        @Override
        public List<List<ValueObjectTypeItemStack.ValueItemStack>> getItemStacksRaw() {
            return itemStacks;
        }

        @Override
        public int getFluidStackIngredients() {
            return fluidStacks.size();
        }

        @Override
        public List<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStacks(int index) {
            return fluidStacks.get(index);
        }

        @Override
        public Predicate<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStackPredicate(int index) {
            return fluidStacks.get(index)::contains;
        }

        @Override
        public List<List<ValueObjectTypeFluidStack.ValueFluidStack>> getFluidStacksRaw() {
            return fluidStacks;
        }

        @Override
        public int getEnergyIngredients() {
            return energies.size();
        }

        @Override
        public List<ValueTypeInteger.ValueInteger> getEnergies(int index) {
            return energies.get(index);
        }

        @Override
        public Predicate<ValueTypeInteger.ValueInteger> getEnergiesPredicate(int index) {
            return energies.get(index)::contains;
        }

        @Override
        public List<List<ValueTypeInteger.ValueInteger>> getEnergiesRaw() {
            return energies;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof IIngredients
                    && this.getItemStacksRaw().equals(((IIngredients) obj).getItemStacksRaw())
                    && this.getFluidStacksRaw().equals(((IIngredients) obj).getFluidStacksRaw())
                    && this.getEnergiesRaw().equals(((IIngredients) obj).getEnergiesRaw()));
        }

        @Override
        public String toString() {
            return "items: " + getItemStacksRaw() + "; fluids: " + getFluidStacksRaw() + "; energies: " + getEnergiesRaw();
        }
    }

}
