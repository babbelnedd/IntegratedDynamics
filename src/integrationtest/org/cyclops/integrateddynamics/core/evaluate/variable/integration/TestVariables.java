package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredientItemStack;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;
import org.cyclops.integrateddynamics.core.test.TestHelpers;

/**
 * Test the different variable types.
 * @author rubensworks
 */
public class TestVariables {

    @IntegrationTest
    public void testIngredientsType() {
        DummyVariableIngredients inull = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(null));
        TestHelpers.assertEqual(inull.getValue().getRawValue().orNull(), null, "null value is null");

        ValueObjectTypeIngredients.IIngredients ingredients1 =
                new ValueObjectTypeIngredients.IngredientsRecipeIngredientsWrapper(new RecipeIngredients(
                        new RecipeIngredientItemStack(Ingredient.EMPTY),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Items.BOAT)),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Item.getItemFromBlock(Blocks.STONE))),
                        new RecipeIngredientItemStack(Ingredient.EMPTY)
                ));
        DummyVariableIngredients i0 = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients
                .of(ingredients1));
        TestHelpers.assertEqual(i0.getValue().getRawValue().get(), ingredients1, "ingredient value is ingredient");

        TestHelpers.assertEqual(i0.getType().serialize(i0.getValue()), "{energies:[],items:[[],[\"{id:\\\"minecraft:boat\\\",Count:1,Damage:32767s}\"],[\"{id:\\\"minecraft:stone\\\",Count:1,Damage:32767s}\"],[]],fluids:[]}", "Serialization is correct");
        TestHelpers.assertEqual(i0.getType().deserialize("{energies:[],items:[[],[\"{id:\\\"minecraft:boat\\\",Count:1,Damage:32767s}\"],[\"{id:\\\"minecraft:stone\\\",Count:1,Damage:32767s}\"],[]],fluids:[]}"), i0.getValue(), "Deserialization is correct");
    }

}
