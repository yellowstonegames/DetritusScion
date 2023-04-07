#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

// Daltonize (source http://www.daltonize.org/search/label/Daltonize)
// Modified to attempt to correct colors so people with color blindness can recognize them.
// Gist by Jonathan Dickinson, from https://gist.github.com/jcdickinson/580b7fb5cc145cee8740

// 0 is no change (full color), 1 corrects for protanopia, 2 corrects for deuteranopia, 3 corrects for tritanopia.
#define mode 0

vec4 correct(vec4 color)
{
	// RGB to LMS matrix conversion
	float L = (17.8824 * color.r) + (43.5161 * color.g) + (4.11935 * color.b);
	float M = (3.45565 * color.r) + (27.1554 * color.g) + (3.86714 * color.b);
	float S = (0.0299566 * color.r) + (0.184309 * color.g) + (1.46709 * color.b);
    
	// Simulate color blindness
    float l;
    float m;
    float s;
	#if (mode == 1) // Protanope - reds are greatly reduced (1% men)
		l = 0.0 * L + 2.02344 * M + -2.52581 * S;
		m = 0.0 * L + 1.0 * M + 0.0 * S;
		s = 0.0 * L + 0.0 * M + 1.0 * S;
	#elif (mode == 2) // Deuteranope - greens are greatly reduced (1% men)
		l = 1.0 * L + 0.0 * M + 0.0 * S;
		m = 0.494207 * L + 0.0 * M + 1.24827 * S;
		s = 0.0 * L + 0.0 * M + 1.0 * S;
	#elif (mode == 3) // Tritanope - blues are greatly reduced (0.003% population)
		l = 1.0 * L + 0.0 * M + 0.0 * S;
		m = 0.0 * L + 1.0 * M + 0.0 * S;
		s = -0.395913 * L + 0.801109 * M + 0.0 * S;
	#else
	    l = L;
        m = M;
        s = S;
    #endif
    
	// LMS to RGB matrix conversion
	vec4 error;
	error.r = (0.0809444479 * l) + (-0.130504409 * m) + (0.116721066 * s);
	error.g = (-0.0102485335 * l) + (0.0540193266 * m) + (-0.113614708 * s);
	error.b = (-0.000365296938 * l) + (-0.00412161469 * m) + (0.693511405 * s);
	error.a = color.a;

	//return error;

	//// Corrections for a specific mode of colorblindness:

	// Isolate invisible colors to color vision deficiency (calculate error matrix)
	error = (color - error);
	
	// Shift colors towards visible spectrum (apply error modifications)
	vec4 correction;
	correction.r = 0; // (error.r * 0.0) + (error.g * 0.0) + (error.b * 0.0);
	correction.g = (error.r * 0.7) + (error.g * 1.0); // + (error.b * 0.0);
	correction.b = (error.r * 0.7) + (error.b * 1.0); // + (error.g * 0.0);
	
	// Add compensation to original values
	correction.rgb = clamp(color.rgb + correction.rgb, 0.0, 1.0);
	correction.a = color.a;
	
	return correction;
}

void main()
{
  gl_FragColor = correct(v_color * texture2D(u_texture, v_texCoords));
}