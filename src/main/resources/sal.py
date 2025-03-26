import cv2
import numpy as np
import random

def salt_and_pepper_noise(image, salt_prob, pepper_prob):
    """
    Add salt and pepper noise to a grayscale image.

    :param image: Input grayscale image.
    :param salt_prob: Probability of salt noise (white pixels).
    :param pepper_prob: Probability of pepper noise (black pixels).
    :return: Noisy image.
    """
    noisy_image = image.copy()
    total_pixels = image.size

    # Salt noise (white pixels)
    num_salt = int(salt_prob * total_pixels)
    salt_coords = [(
        random.randint(0, image.shape[0] - 1),
        random.randint(0, image.shape[1] - 1)
    ) for _ in range(num_salt)]
    
    for coord in salt_coords:
        noisy_image[coord[0], coord[1]] = 255  # Set to white

    # Pepper noise (black pixels)
    num_pepper = int(pepper_prob * total_pixels)
    pepper_coords = [(
        random.randint(0, image.shape[0] - 1),
        random.randint(0, image.shape[1] - 1)
    ) for _ in range(num_pepper)]
    
    for coord in pepper_coords:
        noisy_image[coord[0], coord[1]] = 0  # Set to black

    return noisy_image

# Add Salt and Pepper noise to OpenCV image, vectorized approach.
# https://stackoverflow.com/questions/22937589/how-to-add-noise-gaussian-salt-and-pepper-etc-to-image-in-python-with-opencv
# Forked and fixed from https://gist.github.com/lucaswiman/1e877a164a69f78694f845eab45c381a
# Fixed: replaced 'image' with 'output'

import numpy as np
import cv2


def sp_noise(image, prob):
    '''
    Add salt and pepper noise to image
    prob: Probability of the noise
    '''
    output = image.copy()
    if len(image.shape) == 2:
        black = 0
        white = 255            
    else:
        colorspace = image.shape[2]
        if colorspace == 3:  # RGB
            black = np.array([0, 0, 0], dtype='uint8')
            white = np.array([255, 255, 255], dtype='uint8')
        else:  # RGBA
            black = np.array([0, 0, 0, 255], dtype='uint8')
            white = np.array([255, 255, 255, 255], dtype='uint8')
    probs = np.random.random(output.shape[:2])
    output[probs < (prob / 2)] = black
    output[probs > 1 - (prob / 2)] = white
    return output

# Read the image in grayscale
image = cv2.imread('brightened_image.jpg', cv2.IMREAD_GRAYSCALE)

# Add salt and pepper noise to the grayscale image (e.g., 1% salt, 1% pepper)
noisy_image = sp_noise(image, prob=0.1)

# Show the noisy image
cv2.imshow('Noisy Grayscale Image', noisy_image)
cv2.waitKey(0)
cv2.destroyAllWindows()

# Optionally, save the noisy image
cv2.imwrite('noisy_grayscale_image.jpg', noisy_image)
