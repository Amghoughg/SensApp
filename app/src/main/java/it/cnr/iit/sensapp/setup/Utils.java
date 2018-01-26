package it.cnr.iit.sensapp.setup;

import android.content.Context;

import com.dd.morphingbutton.MorphingButton;

import it.cnr.iit.sensapp.R;

/**
 * Created by mattia on 15/12/17.
 */

public class Utils {

    public static void morphToSquare(Context context, final MorphingButton btnMorph, int duration,
                                     String text) {

        MorphingButton.Params square = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius((int) context.getResources().getDimension(R.dimen.button_normal_corner_radius))
                .width((int) context.getResources().getDimension(R.dimen.button_width))
                .height((int) context.getResources().getDimension(R.dimen.button_height))
                .color(context.getResources().getColor(R.color.colorPrimary))
                .color(context.getResources().getColor(R.color.colorPrimaryDark))
                .text(text);
        btnMorph.morph(square);
    }

    public static void morphToSquareSide(Context context, final MorphingButton btnMorph,
                                         int duration, String text) {

        MorphingButton.Params square = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius((int) context.getResources().getDimension(R.dimen.button_normal_corner_radius))
                .width(0)
                .height((int) context.getResources().getDimension(R.dimen.button_height))
                .color(context.getResources().getColor(R.color.colorPrimary))
                .color(context.getResources().getColor(R.color.colorPrimaryDark))
                .text(text);
        btnMorph.morph(square);
    }

    public static void morphToGreenSquareSide(Context context, final MorphingButton btnMorph,
                                         int duration, String text) {

        MorphingButton.Params square = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius((int) context.getResources().getDimension(R.dimen.button_normal_corner_radius))
                .width(0)
                .height((int) context.getResources().getDimension(R.dimen.button_height))
                .color(context.getResources().getColor(R.color.disabled_green))
                .color(context.getResources().getColor(R.color.disabled_green))
                .text(text);
        btnMorph.morph(square);
    }

    public static void enableGreen(Context context, final MorphingButton btnMorph, String text) {

        MorphingButton.Params square = MorphingButton.Params.create()
                .duration(0)
                .cornerRadius((int) context.getResources().getDimension(R.dimen.button_normal_corner_radius))
                .color(context.getResources().getColor(R.color.light_green))
                .color(context.getResources().getColor(R.color.dark_green))
                .text(text);
        btnMorph.morph(square);
    }

    public static void morphToFailure(Context context, final MorphingButton btnMorph, int duration) {

        MorphingButton.Params circle = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius((int) context.getResources().getDimension(R.dimen.button_round))
                .width((int) context.getResources().getDimension(R.dimen.button_round))
                .height((int) context.getResources().getDimension(R.dimen.button_round))
                .color(context.getResources().getColor(R.color.light_red))
                .colorPressed(context.getResources().getColor(R.color.dark_red))
                .icon(R.drawable.ic_action_error);
        btnMorph.morph(circle);
    }

    public static void morphToSuccess(Context context, final MorphingButton btnMorph, int duration) {

        MorphingButton.Params circle = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius((int) context.getResources().getDimension(R.dimen.button_round))
                .width((int) context.getResources().getDimension(R.dimen.button_round))
                .height((int) context.getResources().getDimension(R.dimen.button_round))
                .color(context.getResources().getColor(R.color.light_green))
                .colorPressed(context.getResources().getColor(R.color.dark_green))
                .icon(R.drawable.ic_action_done);
        btnMorph.morph(circle);
    }
}
