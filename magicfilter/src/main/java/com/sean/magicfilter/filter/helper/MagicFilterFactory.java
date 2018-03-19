package com.sean.magicfilter.filter.helper;

import com.sean.magicfilter.filter.advanced.MagicAmaroFilter;
import com.sean.magicfilter.filter.advanced.MagicAntiqueFilter;
import com.sean.magicfilter.filter.advanced.MagicBlackCatFilter;
import com.sean.magicfilter.filter.advanced.MagicBrannanFilter;
import com.sean.magicfilter.filter.advanced.MagicBrooklynFilter;
import com.sean.magicfilter.filter.advanced.MagicCalmFilter;
import com.sean.magicfilter.filter.advanced.MagicCoolFilter;
import com.sean.magicfilter.filter.advanced.MagicCrayonFilter;
import com.sean.magicfilter.filter.advanced.MagicEarlyBirdFilter;
import com.sean.magicfilter.filter.advanced.MagicEmeraldFilter;
import com.sean.magicfilter.filter.advanced.MagicEvergreenFilter;
import com.sean.magicfilter.filter.advanced.MagicFairytaleFilter;
import com.sean.magicfilter.filter.advanced.MagicFreudFilter;
import com.sean.magicfilter.filter.advanced.MagicHealthyFilter;
import com.sean.magicfilter.filter.advanced.MagicHefeFilter;
import com.sean.magicfilter.filter.advanced.MagicHudsonFilter;
import com.sean.magicfilter.filter.advanced.MagicImageAdjustFilter;
import com.sean.magicfilter.filter.advanced.MagicInkwellFilter;
import com.sean.magicfilter.filter.advanced.MagicKevinFilter;
import com.sean.magicfilter.filter.advanced.MagicLatteFilter;
import com.sean.magicfilter.filter.advanced.MagicLomoFilter;
import com.sean.magicfilter.filter.advanced.MagicN1977Filter;
import com.sean.magicfilter.filter.advanced.MagicNashvilleFilter;
import com.sean.magicfilter.filter.advanced.MagicNostalgiaFilter;
import com.sean.magicfilter.filter.advanced.MagicPixarFilter;
import com.sean.magicfilter.filter.advanced.MagicRiseFilter;
import com.sean.magicfilter.filter.advanced.MagicRomanceFilter;
import com.sean.magicfilter.filter.advanced.MagicSakuraFilter;
import com.sean.magicfilter.filter.advanced.MagicSierraFilter;
import com.sean.magicfilter.filter.advanced.MagicSketchFilter;
import com.sean.magicfilter.filter.advanced.MagicSkinWhitenFilter;
import com.sean.magicfilter.filter.advanced.MagicSunriseFilter;
import com.sean.magicfilter.filter.advanced.MagicSunsetFilter;
import com.sean.magicfilter.filter.advanced.MagicSutroFilter;
import com.sean.magicfilter.filter.advanced.MagicSweetsFilter;
import com.sean.magicfilter.filter.advanced.MagicTenderFilter;
import com.sean.magicfilter.filter.advanced.MagicToasterFilter;
import com.sean.magicfilter.filter.advanced.MagicValenciaFilter;
import com.sean.magicfilter.filter.advanced.MagicWaldenFilter;
import com.sean.magicfilter.filter.advanced.MagicWarmFilter;
import com.sean.magicfilter.filter.advanced.MagicWhiteCatFilter;
import com.sean.magicfilter.filter.advanced.MagicXproIIFilter;
import com.sean.magicfilter.filter.base.gpuimage.GPUImageBrightnessFilter;
import com.sean.magicfilter.filter.base.gpuimage.GPUImageContrastFilter;
import com.sean.magicfilter.filter.base.gpuimage.GPUImageExposureFilter;
import com.sean.magicfilter.filter.base.gpuimage.GPUImageFilter;
import com.sean.magicfilter.filter.base.gpuimage.GPUImageHueFilter;
import com.sean.magicfilter.filter.base.gpuimage.GPUImageSaturationFilter;
import com.sean.magicfilter.filter.base.gpuimage.GPUImageSharpenFilter;

public class MagicFilterFactory{
	
	private static MagicFilterType filterType = MagicFilterType.NONE;
	
	public static GPUImageFilter initFilters(MagicFilterType type){
		filterType = type;
		switch (type) {
		case WHITECAT:
			return new MagicWhiteCatFilter();
		case BLACKCAT:
			return new MagicBlackCatFilter();
		case SKINWHITEN:
			return new MagicSkinWhitenFilter();
		case ROMANCE:
			return new MagicRomanceFilter();
		case SAKURA:
			return new MagicSakuraFilter();
		case AMARO:
			return new MagicAmaroFilter();
		case WALDEN:
			return new MagicWaldenFilter();
		case ANTIQUE:
			return new MagicAntiqueFilter();
		case CALM:
			return new MagicCalmFilter();
		case BRANNAN:
			return new MagicBrannanFilter();
		case BROOKLYN:
			return new MagicBrooklynFilter();
		case EARLYBIRD:
			return new MagicEarlyBirdFilter();
		case FREUD:
			return new MagicFreudFilter();
		case HEFE:
			return new MagicHefeFilter();
		case HUDSON:
			return new MagicHudsonFilter();
		case INKWELL:
			return new MagicInkwellFilter();
		case KEVIN:
			return new MagicKevinFilter();
		case LOMO:
			return new MagicLomoFilter();
		case N1977:
			return new MagicN1977Filter();
		case NASHVILLE:
			return new MagicNashvilleFilter();
		case PIXAR:
			return new MagicPixarFilter();
		case RISE:
			return new MagicRiseFilter();
		case SIERRA:
			return new MagicSierraFilter();
		case SUTRO:
			return new MagicSutroFilter();
		case TOASTER2:
			return new MagicToasterFilter();
		case VALENCIA:
			return new MagicValenciaFilter();
		case XPROII:
			return new MagicXproIIFilter();
		case EVERGREEN:
			return new MagicEvergreenFilter();
		case HEALTHY:
			return new MagicHealthyFilter();
		case COOL:
			return new MagicCoolFilter();
		case EMERALD:
			return new MagicEmeraldFilter();
		case LATTE:
			return new MagicLatteFilter();
		case WARM:
			return new MagicWarmFilter();
		case TENDER:
			return new MagicTenderFilter();
		case SWEETS:
			return new MagicSweetsFilter();
		case NOSTALGIA:
			return new MagicNostalgiaFilter();
		case FAIRYTALE:
			return new MagicFairytaleFilter();
		case SUNRISE:
			return new MagicSunriseFilter();
		case SUNSET:
			return new MagicSunsetFilter();
		case CRAYON:
			return new MagicCrayonFilter();
		case SKETCH:
			return new MagicSketchFilter();
		//image adjust
		case BRIGHTNESS:
			return new GPUImageBrightnessFilter();
		case CONTRAST:
			return new GPUImageContrastFilter();
		case EXPOSURE:
			return new GPUImageExposureFilter();
		case HUE:
			return new GPUImageHueFilter();
		case SATURATION:
			return new GPUImageSaturationFilter();
		case SHARPEN:
			return new GPUImageSharpenFilter();
		case IMAGE_ADJUST:
			return new MagicImageAdjustFilter();
		default:
			return null;
		}
	}
	
	public MagicFilterType getCurrentFilterType(){
		return filterType;
	}
}
