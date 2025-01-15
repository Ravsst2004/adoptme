<?php

namespace App\Http\Controllers;

use App\Models\Booking;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;

class BookingController extends Controller
{
    public function index()
    {
        $bookings = Booking::with('user', 'pet')
            ->where('status', 'active')
            ->get();
        return response()->json([
            'status' => true,
            'message' => 'Bookings retrieved successfully.',
            'data' => $bookings,
        ]);
    }

    public function show(Request $request)
    {
        $userId = $request->query('user_id');

        Log::info('Received show booking request: ', compact('userId'));

        if ($userId) {
            $request->validate([
                'user_id' => 'required|exists:users,id'
            ]);

            $booking = Booking::where('user_id', $userId)->first();

            if (!$booking) {
                Log::warning('Booking not found for user ID: ' . $userId);

                return response()->json([
                    'status' => false,
                    'message' => 'Booking Not Found.',
                ]);
            }

            Log::info('Booking found for user ID: ' . $userId);

            return response()->json([
                'status' => true,
                'message' => 'Bookings retrieved successfully.',
                'data' => $booking,
            ]);
        }

        $bookings = Booking::all();

        return response()->json([
            'status' => true,
            'message' => 'Bookings retrieved successfully.',
            'data' => $bookings,
        ]);
    }

    public function store(Request $request)
    {
        Log::info('Received booking request: ', $request->all());

        $request->validate([
            'pet_id' => 'required|exists:pets,id',
            'user_id' => 'required|exists:users,id'
        ]);

        $userId = $request->user_id;
        $petId = $request->pet_id;

        if (!$petId) {
            Log::error("Booking request failed: Pet ID is missing.");

            return response()->json([
                'status' => false,
                'message' => 'Pet Not Found.',
            ]);
        }

        if (!$userId) {
            Log::error("Booking request failed: User ID is missing.");

            return response()->json([
                'status' => false,
                'message' => 'User Not Found.',
            ]);
        }

        $existingBooking = Booking::where('pet_id', $petId)->first();
        if ($existingBooking) {
            Log::error("Booking request failed: Pet $petId is already booked by another user.");

            return response()->json([
                'status' => false,
                'message' => 'This pet is already booked by another user.',
            ], 400);
        }

        $userBooking = Booking::where('user_id', $userId)->first();
        if ($userBooking) {
            Log::error("Booking request failed: User $userId already has an active booking.");

            return response()->json([
                'status' => false,
                'message' => 'You already have an active booking.',
            ], 400);
        }

        try {
            $booking = Booking::create([
                'user_id' => $userId,
                'pet_id' => $petId,
            ]);

            Log::info("Booking request successful: $userId booked $petId");

            return response()->json([
                'status' => true,
                'message' => 'Booking successful!',
                'data' => $booking,
            ]);
        } catch (\Exception $e) {
            Log::error("Booking request failed: " . $e->getMessage());

            return response()->json([
                'status' => false,
                'message' => 'Internal Server Error',
            ], 500);
        }
    }

    public function destroy(Booking $booking)
    {
        try {
            $booking->delete();
            return response()->json([
                'status' => true,
                'message' => 'Booking deleted successfully',
                'data' => $booking
            ]);
        } catch (\Exception $e) {
            Log::error('Error deleting booking: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Failed to delete booking'
            ], 500);
        }
    }

    public function updateStatus($id)
    {
        try {
            $booking = Booking::findOrFail($id);
            $booking->status = 'completed';
            $booking->save();

            return response()->json([
                'status' => true,
                'message' => 'Booking status updated successfully',
                'data' => $booking
            ]);
        } catch (\Exception $e) {
            Log::error('Error updating booking status: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Failed to update booking status'
            ], 500);
        }
    }
}
